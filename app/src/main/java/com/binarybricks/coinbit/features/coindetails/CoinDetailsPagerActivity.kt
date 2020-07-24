package com.binarybricks.coinbit.features.coindetails

import CoinDetailsPagerContract
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.binarybricks.coinbit.CoinBitApplication
import com.binarybricks.coinbit.R
import com.binarybricks.coinbit.data.database.entities.WatchedCoin
import com.binarybricks.coinbit.network.schedulers.RxSchedulers
import kotlinx.android.synthetic.main.activity_pager_coin_details.*
import kotlinx.android.synthetic.main.fragment_coin_details.*
import timber.log.Timber

class CoinDetailsPagerActivity : AppCompatActivity(), CoinDetailsPagerContract.View {

    private var watchedCoin: WatchedCoin? = null
    var isCoinInfoChanged = false

    private val rxSchedulers: RxSchedulers by lazy {
        RxSchedulers.instance
    }

    private val allCoinDetailsRepository by lazy {
        CoinDetailsPagerRepository(rxSchedulers, CoinBitApplication.database)
    }

    private val coinDetailPagerPresenter: CoinDetailPagerPresenter by lazy {
        CoinDetailPagerPresenter(rxSchedulers, allCoinDetailsRepository)
    }

    companion object {
        private const val WATCHED_COIN = "WATCHED_COIN"

        @JvmStatic
        fun buildLaunchIntent(context: Context, watchedCoin: WatchedCoin): Intent {
            val intent = Intent(context, CoinDetailsPagerActivity::class.java)
            intent.putExtra(WATCHED_COIN, watchedCoin)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager_coin_details)

        toolbar.elevation = 0f

        val toolbar = findViewById<View>(R.id.toolbar)
        setSupportActionBar(toolbar as Toolbar?)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        watchedCoin = intent.getParcelableExtra(WATCHED_COIN)

        coinDetailPagerPresenter.attachView(this)

        lifecycle.addObserver(coinDetailPagerPresenter)

        showOrHideLoadingIndicator(true)

        coinDetailPagerPresenter.loadWatchedCoins()

        Timber.i("CoinDetailsPagerActivity")
    }

    override fun onWatchedCoinsLoaded(watchedCoinList: List<WatchedCoin>?) {

        supportActionBar?.title = getString(R.string.transactionTypeWithQuantity,
                watchedCoin?.coin?.coinName, watchedCoin?.coin?.symbol)

        val allCoinsPagerAdapter = CoinDetailsPagerAdapter(watchedCoinList, supportFragmentManager)
        vpCoins.adapter = allCoinsPagerAdapter

        showOrHideLoadingIndicator(false)

        watchedCoinList?.forEachIndexed { index, watch ->
            if (watchedCoin?.coin?.name == watch.coin.name) {
                vpCoins.currentItem = index
            }
        }

        vpCoins.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                supportActionBar?.title = watchedCoinList?.get(position)?.coin?.coinName
            }
        })
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(rvCoinDetails, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun showOrHideLoadingIndicator(showLoading: Boolean) {
        if (showLoading) {
            pbLoading.visibility = View.VISIBLE
        } else {
            pbLoading.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isCoinInfoChanged) {
            setResult(Activity.RESULT_OK)
        }

        finish()
    }
}
