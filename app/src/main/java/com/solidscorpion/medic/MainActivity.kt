package com.solidscorpion.medic

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.http.SslError
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.solidscorpion.medic.adapter.CustomArrayAdapter
import com.solidscorpion.medic.adapter.RVAdapter
import com.solidscorpion.medic.databinding.ActivityMainBinding
import com.solidscorpion.medic.pojo.BaseItem
import com.solidscorpion.medic.pojo.ModelMenuItem
import kotlinx.android.synthetic.main.activity_main.view.*
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity(), MainActivityContract.View, AppBarLayout.OnOffsetChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val FULL_TOOLBAR_HEIGHT_DP = 101
    private val SMALL_TOOLBAR_HEIGHT_DP = 50

    private var loggedUser = false
    private lateinit var presenter: MainActivityContract.Presenter
    private var isMenuOpened = false
    private var toolbarOffset = 0

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceRTLIfSupported()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.appbar.addOnOffsetChangedListener(this)
        val application = (application as MedicApplication)
        presenter = MainActivityPresenter(this, application.api)
        presenter.loadMenuItems(loggedUser)
        optimizeWebView()
        binding.webview.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.pbLoading.visibility = View.VISIBLE
                if (url != "https://medic.co.il/?app") {
                    binding.toolbar.btnShare.visibility = View.VISIBLE
                    binding.toolbar.imgBack.visibility = View.VISIBLE
                    setCustomToolbar(FULL_TOOLBAR_HEIGHT_DP)
                } else {
                    binding.toolbar.btnShare.visibility = View.GONE
                    binding.toolbar.imgBack.visibility = View.GONE
                    setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
                }
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                binding.pbLoading.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val aURL = URL(url.toString())
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.hasGesture()) return false
                if (request.url.toString().contains("/jwt-auth/v1/token")) {
                    val aURL = URL(request.url.toString())
                    val conn = aURL.openConnection()
                    conn.connect()
                    val input = conn.getInputStream()
                    val jsonParser = JsonParser()
                    val jsonObject = jsonParser.parse(InputStreamReader(input, "UTF-8"))
                    val s = jsonObject.toString()
                }
                if (!request.url.toString().contains("?")) {
                    binding.webview.loadUrl(
                        StringBuilder()
                            .append(request.url)
                            .append("?app")
                            .toString()
                    )
                } else {
                    binding.webview.loadUrl(
                        StringBuilder()
                            .append(request.url)
                            .append("&app")
                            .toString()
                    )
                }
                return true
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
                super.onReceivedSslError(view, handler, error)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                if (request?.url.toString().equals("https://medic.co.il/wp-json/medic/v1/page")) {
                    val token = request?.requestHeaders?.get("Authorization")
                    if (token != null && token.isNotEmpty()) (presenter as MainActivityPresenter).validateToken(token)
                }
                return super.shouldInterceptRequest(view, request)
            }

        }
        binding.toolbar.autocomplete.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                binding.toolbar.autocomplete.hint = ""
            else
                binding.toolbar.autocomplete.hint = getString(R.string.search_israel_drug_index)
        }
        binding.toolbar.autocomplete.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(v.text)) {
                onSearchClicked()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.toolbar.btnShare.setOnClickListener { onShareClicked(binding.webview.url) }
        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        settings.setDomStorageEnabled(true)
        settings.databaseEnabled = true
        binding.webview.loadUrl("https://medic.co.il/?app")
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
        }
        binding.toolbar.autocomplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""
                if (!text.isEmpty()) {
                    binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.new_close))
                    presenter.performSearch(text, 0)
                } else {
                    binding.toolbar.searchIcon.setImageDrawable(getDrawable(R.drawable.ic_search_blue))
                    if (binding.toolbar.autocomplete.adapter != null) {
                        (binding.toolbar.autocomplete.adapter as CustomArrayAdapter).clear()
                    }
                }
            }
        })
        binding.drawerLayout.menu.layoutManager = LinearLayoutManager(this)

        binding.toolbar.searchIcon.setOnClickListener {
            if (!TextUtils.isEmpty(binding.toolbar.autocomplete.text)) {
                (autocomplete.adapter as CustomArrayAdapter).clear()
                binding.toolbar.autocomplete.setText("")
            }
        }
        binding.toolbar.imgBack.setOnClickListener { onBackPressed() }
        binding.btnUser.setOnClickListener { onUserIconClicked() }
        binding.toolbar.btnHome.setOnClickListener {
            isMenuOpened = if (isMenuOpened) {
                hideKeyboard()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isAcceptingText) {
                    Handler().postDelayed({
                        if (binding.webview.url == "https://medic.co.il/?app") {
                            setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
                        }
                        slideUp(binding.drawerLayout.drawerContainer)
                    }, 1000)
                } else {
                    if (binding.webview.url == "https://medic.co.il/?app") {
                        setCustomToolbar(SMALL_TOOLBAR_HEIGHT_DP)
                    }
                    slideUp(binding.drawerLayout.drawerContainer)
                }
                false
            } else {
                hideKeyboard()
                if (binding.webview.url == "https://medic.co.il/?app") {
                    setCustomToolbar(FULL_TOOLBAR_HEIGHT_DP)
                }
                slideDown(binding.drawerLayout.drawerContainer)
                true
            }
        }
    }

    private fun optimizeWebView() {
        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = false
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.domStorageEnabled = true
        binding.webview.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        binding.webview.isScrollbarFadingEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            binding.webview.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            binding.webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun onSearchClicked() {
        hideKeyboard()
        isMenuOpened = false
        presenter.onStop()
        Handler().postDelayed({
            loadEmptySearch()
            clearSearch()
        }, 1000)
    }

    private fun clearSearch() {
        binding.toolbar.autocomplete.setText("")
        binding.toolbar.autocomplete.clearFocus()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

    override fun showResults(results: List<BaseItem>) {
        if (autocomplete.text.isEmpty()) (results as ArrayList).clear()
        val adapter = CustomArrayAdapter(this, results)
        binding.toolbar.autocomplete.setAdapter(adapter)
        adapter.notifyDataSetChanged()
        binding.toolbar.autocomplete.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                hideKeyboard()
                presenter.onStop()
                isMenuOpened = false
                val selectedItem = adapter.getBaseItem(p2)
                clearSearch()
                Handler().postDelayed({
                    binding.webview.loadUrl(selectedItem.url)
                    slideUp(binding.drawerLayout.drawerContainer)
                }, 1000)
            }
        }
    }

    private fun hideKeyboard() {
        val input = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(
            autocomplete
                .applicationWindowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun loadEmptySearch() {
        slideUp(binding.drawerLayout.drawerContainer)
        binding.webview.loadUrl("https://medic.co.il/medic-search/${binding.toolbar.autocomplete.text}".plus("/?app"))
    }

    private fun disableScroll() {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = 0
        appbar.requestLayout()
    }

    private fun enableScroll() {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        appbar.requestLayout()
    }

    private fun setCustomToolbar(dp: Int) {
        if (dp == FULL_TOOLBAR_HEIGHT_DP) {
            toolbarOffset = calcOffset(FULL_TOOLBAR_HEIGHT_DP)
            binding.toolbar.autocomplete.visibility = View.VISIBLE
            binding.toolbar.searchIcon.visibility = View.VISIBLE
        } else {
            toolbarOffset = calcOffset(SMALL_TOOLBAR_HEIGHT_DP)
            binding.toolbar.autocomplete.visibility = View.GONE
            binding.toolbar.searchIcon.visibility = View.GONE
        }
        setToolbarHeight(dp)
    }

    private fun setToolbarHeight(dp: Int) {
        val params = toolbar.layoutParams as AppBarLayout.LayoutParams
        var scale = resources.displayMetrics.density
        val pixels = (dp * scale + 0.5f)
        params.height = pixels.toInt()
        appbar.setExpanded(true)
        appbar.requestLayout()
        appbar.invalidate()
    }

    private fun calcOffset(dp: Int): Int {
        var scale = resources.displayMetrics.density
        val pixels = (dp * scale + 0.5f)
        return pixels.toInt() * -1
    }

    override fun showProgress() {
//        binding.toolbar.toolsearchProgress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
//        binding.toolbar.toolsearchProgress.visibility = View.GONE
    }

    private fun onShareClicked(url: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, url.substring(0, url.length - 5))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share using?"))
    }

    open fun openMain(view: View) {
        hideKeyboard()
        Handler().postDelayed({
            isMenuOpened = false
            slideUp(binding.drawerLayout.drawerContainer)
        }, 1000)
        binding.webview.loadUrl("https://medic.co.il/?app")
        presenter.loadMenuItems(loggedUser)
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }

    override fun onMenuItemsLoaded(items: List<ModelMenuItem>) {
        val adapter = RVAdapter(this, items,
            onClick = {
                if (it.link.length > 1 || it.link == "/") {
                    binding.webview.loadUrl(
                        StringBuilder()
                            .append("https://medic.co.il")
                            .append(it.link)
                            .toString()
                    )
                    slideUp(binding.drawerLayout.drawerContainer)
                    isMenuOpened = false
                }
            },
            onSignUpClick = {
                binding.webview.loadUrl(
                    StringBuilder()
                        .append("https://medic.co.il/subscribe")
                        .append("/?app")
                        .toString()
                )
                slideUp(binding.drawerLayout.drawerContainer)
                isMenuOpened = false
            },
            onAccountClick = {
                binding.webview.loadUrl(
                    StringBuilder()
                        .append("https://medic.co.il/my-account")
                        .append("/?app")
                        .toString()
                )
                slideUp(binding.drawerLayout.drawerContainer)
                isMenuOpened = false
            },
            onSingOutClick = {
                loggedUser = false
                logoutUser()
                binding.btnUser.setImageDrawable(getDrawable(R.drawable.ic_user))
                presenter.loadMenuItems(loggedUser)
                slideUp(binding.drawerLayout.drawerContainer)
                isMenuOpened = false
            })
        binding.drawerLayout.menu.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                slideDown(binding.drawerLayout.drawerContainer)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else super.onBackPressed()
    }

    private fun slideUp(view: View) {
        enableScroll()
        view.animate().translationY(-view.height.toFloat()).setDuration(500).start()
        binding.toolbar.btnHome.setImageResource(R.drawable.new_menu)
    }

    private fun slideDown(view: View) {
        disableScroll()
        view.animate().translationY(0f).setDuration(500).start()
        binding.toolbar.btnHome.setImageResource(R.drawable.new_close)
    }

    private fun onUserIconClicked() {
        if (!loggedUser) {
            Toast.makeText(this, "loginPopup.open() TRIGGERED", Toast.LENGTH_SHORT).show()
            binding.webview.loadUrl("javascript:loginPopup.open()")
        }
    }

    private fun logoutUser() {
        binding.webview.loadUrl("javascript:loginPopup.signOut()")
    }

    override fun userLogged()  {
        loggedUser = true
//        binding.btnUser.setImageDrawable(getDrawable(R.drawable.ic_user_logged))
        binding.btnUser.visibility = View.GONE
        presenter.loadMenuItems(true)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == toolbarOffset) {
            binding.webview.loadUrl("javascript:ShowInfoLineHeader()")
        } else if (verticalOffset > toolbarOffset) {
            binding.webview.loadUrl("javascript:HideInfoLineHeader()")
        }
    }

}
