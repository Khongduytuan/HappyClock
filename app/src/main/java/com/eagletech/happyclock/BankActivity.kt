package com.eagletech.happyclock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.eagletech.happyclock.dataUser.ManagerData
import com.eagletech.happyclock.databinding.ActivityBankBinding

class BankActivity : AppCompatActivity() {
    private lateinit var sBinding: ActivityBankBinding
    private lateinit var myData: ManagerData
    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String

    // Phải thêm sku các gói vào ứng dụng
    companion object {
        const val use5 = "com.eagletech.happyclock.use5"
        const val use10 = "com.eagletech.happyclock.use10"
        const val use15 = "com.eagletech.happyclock.use15"
        const val subApp = "com.eagletech.happyclock.subapp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityBankBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        myData = ManagerData.getInstance(this)
        setupIAPOnCreate()
        setClickItems()

    }

    private fun setClickItems() {
        sBinding.use5.setOnClickListener {
//            myData.addData(2)
            PurchasingService.purchase(use5)
        }
        sBinding.use10.setOnClickListener {
            PurchasingService.purchase(use10)
        }
        sBinding.use15.setOnClickListener {
            PurchasingService.purchase(use15)
        }
        sBinding.subApp.setOnClickListener {
            PurchasingService.purchase(subApp)
        }
        sBinding.finish.setOnClickListener { finish() }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        myData.userId(currentUserId)
                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED -> Log.v(
                        "IAP SDK",
                        "loading failed"
                    )
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )
                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                when (purchaseResponse.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {

                        if (purchaseResponse.receipt.sku == use5) {
                            myData.addData(5)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == use10) {
                            myData.addData(10)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == use15) {
                            myData.addData(15)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == subApp) {
                            myData.isPremium = true
                            finish()
                        }
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId, FulfillmentResult.FULFILLED
                        )

                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                // Process receipts
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            myData.isPremium = !receipt.isCanceled
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }

                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this, purchasingListener)
        Log.d(
            "DetailBuyAct", "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }


    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(subApp)
        productSkus.add(use5)
        productSkus.add(use10)
        productSkus.add(use15)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(false)
    }
}