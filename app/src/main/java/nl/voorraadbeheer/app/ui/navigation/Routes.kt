package nl.voorraadbeheer.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val LOCATIONS = "locations"
    const val SHOPPING_LIST = "shopping_list"
    const val SETTINGS = "settings"
    const val SCAN = "scan"

    const val INVENTORY = "inventory"
    const val INVENTORY_ARG_LOCATION_ID = "locationId"
    const val INVENTORY_ALL = "$INVENTORY?$INVENTORY_ARG_LOCATION_ID={$INVENTORY_ARG_LOCATION_ID}"
    fun inventoryForLocation(locationId: String?) =
        "$INVENTORY?$INVENTORY_ARG_LOCATION_ID=${locationId.orEmpty()}"

    const val ITEM_DETAIL = "item_detail"
    const val ITEM_DETAIL_ARG_ID = "itemId"
    const val ITEM_DETAIL_ROUTE = "$ITEM_DETAIL/{$ITEM_DETAIL_ARG_ID}"
    fun itemDetail(itemId: String) = "$ITEM_DETAIL/$itemId"

    const val ADD_PRODUCT = "add_product"
    const val ADD_PRODUCT_ARG_BARCODE = "barcode"
    const val ADD_PRODUCT_ARG_ITEM_ID = "itemId"
    const val ADD_PRODUCT_ROUTE =
        "$ADD_PRODUCT?$ADD_PRODUCT_ARG_BARCODE={$ADD_PRODUCT_ARG_BARCODE}&$ADD_PRODUCT_ARG_ITEM_ID={$ADD_PRODUCT_ARG_ITEM_ID}"

    /** Nieuw product toevoegen (evt. met barcode uit de scanner, voor de productopzoek-stap). */
    fun addProduct(barcode: String? = null) =
        "$ADD_PRODUCT?$ADD_PRODUCT_ARG_BARCODE=${barcode.orEmpty()}&$ADD_PRODUCT_ARG_ITEM_ID="

    /** Bestaand voorraaditem bewerken. */
    fun editProduct(itemId: String) =
        "$ADD_PRODUCT?$ADD_PRODUCT_ARG_BARCODE=&$ADD_PRODUCT_ARG_ITEM_ID=$itemId"
}
