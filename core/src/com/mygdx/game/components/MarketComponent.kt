package com.mygdx.game.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.MathUtils
import com.mygdx.game.objects.InventoryItem
import com.mygdx.game.util.DefinitionManager
import com.mygdx.game.objects.Transaction

/***
 * Probably will be pretty similar to the inventory component but with specialized functions
 */
class MarketComponent : Component {
    private val itemMap = hashMapOf<String, InventoryItem>()
    private val itemPriceMap = hashMapOf<String, Float>()

    private val percentChange = 0.01f

    /**
     * Sells an item to the market
     */
    fun sellItemToMarket(itemName:String, itemAmount:Int): Transaction {
        val item = itemMap.getOrPut(itemName, { InventoryItem(itemName, 0) })

        //TODO Implement this!
        //Get current average item price (or calculate it based on how much they are selling)
        //Check if the market has enough money to supply them?
        //Check if the market has enough of the item (get the lowest of this or the above)
        //Exchange the money for goods
        //Be on our merry way. Update average prices?

        //But for now we're just gonna lay down some temp code
        val itemPrice = getAveragePriceForSellingToMarket(itemName, itemAmount) //Get the item price
        //The amount the market can take. Either all of the item or the amount bound by the market money
//        val totalPrice = itemAmount*itemPrice //Get the total price for the transaction

        var adjustedPrice = itemPrice
        for(i in 0 until itemAmount)
            adjustedPrice -= adjustedPrice*percentChange

        itemPriceMap.put(itemName, adjustedPrice)

        item.amount += itemAmount //Add the item amount to the market (we are selling TO the market)

//        if(itemAmount > 1)
//            println("Amount is more than 1 $itemAmount")

        return Transaction(itemName, itemAmount, itemPrice)
    }

    /**
     * Buys an item from the market
     */
    fun buyItemFromMarket(itemName:String, itemAmount:Int): Transaction {
        val item = itemMap[itemName] ?: return Transaction(itemName, 0, 0f)

        //But for now we're just gonna lay down some temp code
        val itemPrice = getAveragePriceForBuyingFromMarket(itemName, itemAmount)
        //The amount the market can give away. Either all of the inventory item or the amount requested by the buyer
        val amountAbleToBuy = Math.min(item.amount, itemAmount)

        val adjustedPrice = itemPrice + itemPrice*percentChange
        itemPriceMap.put(itemName, adjustedPrice)

//        if(itemAmount > 1)
//            println("Amount is more than 1 $itemAmount")

        item.amount -= amountAbleToBuy //Subtract the item from the market
        item.outgoing -= amountAbleToBuy //Subtract it also from the outgoing items. This should usually work?
        //TODO WATCH THIS AREA ABOVE, DO ENTITIES ALWAYS BUY A RESERVED ITEM?

        return Transaction(itemName, amountAbleToBuy, itemPrice)
    }

    /**
     * Calculates the average price when selling items to the market
     * @param itemName The name of the item
     * @param itemAmount The amount of the item
     */
    fun getAveragePriceForSellingToMarket(itemName:String, itemAmount:Int):Float{
        //TODO Actually implement this

        return itemPriceMap.getOrPut(itemName, {
            val price = DefinitionManager.resourceMap[itemName]!!.marketPrice.toFloat()
            price + price*0.75f
        }) //Get the price of the item
    }

    /**
     * Calculates the average price when buying items from the market
     * @param itemName The name of the item
     * @param itemAmount The amount of the item
     * @return The average price of the item in the market.
     */
    fun getAveragePriceForBuyingFromMarket(itemName:String, itemAmount:Int = 1):Float{
        //TODO Actually implement this

        return itemPriceMap.getOrPut(itemName, {
            val price = DefinitionManager.resourceMap[itemName]!!.marketPrice.toFloat()
            price + price*0.75f
        }) //Get the price of the item
    }

    /**
     * Reserves an item for pickup in the market (outgoing)
     */
    fun reserveOutgoingItem(itemName:String, itemAmount:Int):Int{
        val item = itemMap.getOrPut(itemName, {InventoryItem(itemName, 0)})

        //Clamp this between the negative outgoing and the total amount we have
        //The negative outgoing allows us to accept negative numbers to reduce the outgoing amount
        val amountToReserve = MathUtils.clamp(itemAmount, 0, item.amount - item.outgoing)
        item.outgoing += amountToReserve //Add to the outgoing
//        item.amount -= amountToReserve //Take away from the available amount
        //If the item is completely empty, remove it
//        if(item.outgoing == 0 && item.amount == 0 && item.incoming == 0)
//            itemMap.remove(itemName)
        return amountToReserve
    }

    /**
     * Reserves an incoming item to the market
     */
    fun reserveIncomingItem(itemName:String, itemAmount:Int):Int{
        val item = itemMap.getOrPut(itemName, { InventoryItem(itemName, 0) })

        //We can't take away more the than the item incoming, but we can add MAX_VALUE (infinite basically)
        val amountToReserve = MathUtils.clamp(itemAmount, -item.incoming, Int.MAX_VALUE)

        item.incoming += amountToReserve
        return amountToReserve
    }

    fun getAmountInclIncoming(name:String):Int{
        if(hasItem(name)) {
            val item = itemMap[name]!!
            return item.amount + item.incoming
        }
        return 0
    }

    /**
     * Gets the amount of the item available (which is the amount not reserved)
     * @param name The name of the item
     */
    fun getAvailableAmount(name:String):Int{
        val item = itemMap[name] ?: return 0
        return item.amount - item.outgoing
    }

    /**
     * @param name The name of the item
     * @return True if the item is in the inventory, false otherwise.
     */
    fun hasItem(name:String):Boolean{
        return itemMap.containsKey(name)
    }

    /**
     * Gets the item amount if available
     * @param name The name of the item
     * @return The amount of the item or 0 if the item doesn't exist.
     */
    fun getAmount(name:String):Int{
        if(hasItem(name))
            return itemMap[name]!!.amount

        return 0
    }

    fun getAllItems():List<InventoryItem>{
        return itemMap.values.toList()
    }
}