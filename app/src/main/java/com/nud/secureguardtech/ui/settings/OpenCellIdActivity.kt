package com.nud.secureguardtech.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nud.secureguardtech.R
import com.nud.secureguardtech.data.Settings
import com.nud.secureguardtech.data.io.IO
import com.nud.secureguardtech.data.io.JSONFactory
import com.nud.secureguardtech.data.io.json.JSONMap
import com.nud.secureguardtech.databinding.ActivityOpenCellIdBinding
import com.nud.secureguardtech.net.OpenCelliDRepository
import com.nud.secureguardtech.net.OpenCelliDSpec
import com.nud.secureguardtech.utils.CellParameters
import com.nud.secureguardtech.utils.Utils.Companion.getGeoURI
import com.nud.secureguardtech.utils.Utils.Companion.getOpenStreetMapLink
import com.nud.secureguardtech.utils.Utils.Companion.openUrl
import com.nud.secureguardtech.utils.Utils.Companion.pasteFromClipboard


class OpenCellIdActivity : AppCompatActivity(), TextWatcher {

    private lateinit var viewBinding: ActivityOpenCellIdBinding

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityOpenCellIdBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        settings =
            JSONFactory.convertJSONSettings(IO.read(JSONMap::class.java, IO.settingsFileName))
        val apiToken = settings.get(Settings.SET_OPENCELLID_API_KEY) as String

        viewBinding.editTextOpenCellIDAPIKey.setText(apiToken)
        viewBinding.editTextOpenCellIDAPIKey.addTextChangedListener(this)

        viewBinding.buttonPaste.setOnClickListener(::onPasteClicked)
        viewBinding.buttonOpenOpenCellIdWebsite.setOnClickListener(::onOpenWebsiteClicked)
        viewBinding.buttonTestOpenCellId.setOnClickListener(::onTestConnectionClicked)

        setupTestConnection(apiToken.isEmpty())
    }

    private fun setupTestConnection(isApiTokenEmpty: Boolean) {
        if (isApiTokenEmpty) {
            viewBinding.buttonTestOpenCellId.isEnabled = false
            viewBinding.textViewTestOpenCellIdResponse.visibility = View.GONE
        } else {
            viewBinding.buttonTestOpenCellId.isEnabled = true
            viewBinding.textViewTestOpenCellIdResponse.text = ""
            viewBinding.textViewTestOpenCellIdResponse.visibility = View.VISIBLE
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(edited: Editable) {
        if (edited === viewBinding.editTextOpenCellIDAPIKey.text) {
            val newToken = edited.toString().trim()
            settings.setNow(Settings.SET_OPENCELLID_API_KEY, newToken)
            setupTestConnection(newToken.isEmpty())
        }
    }

    private fun onPasteClicked(view: View) {
        viewBinding.editTextOpenCellIDAPIKey.setText(pasteFromClipboard(view.context))
    }

    private fun onOpenWebsiteClicked(view: View) {
        openUrl(view.context, "https://opencellid.org/")
    }

    private fun onTestConnectionClicked(view: View) {
        val context = view.context

        val paras = CellParameters.queryCellParametersFromTelephonyManager(context)
        if (paras == null) {
            Log.i(TAG, "No cell location found")
            viewBinding.textViewTestOpenCellIdResponse.text =
                context.getString(R.string.OpenCellId_test_no_connection)
            return
        }

        val repo = OpenCelliDRepository.getInstance(OpenCelliDSpec(context))
        val apiAccessToken = settings.get(Settings.SET_OPENCELLID_API_KEY) as String

        repo.getCellLocation(
            paras, apiAccessToken,
            onSuccess = {
                val geoURI = getGeoURI(it.lat, it.lon)
                val osm = getOpenStreetMapLink(it.lat, it.lon)
                viewBinding.textViewTestOpenCellIdResponse.text =
                    "Paras: $paras\n\nOpenCelliD: ${it.url}\n${geoURI}\nOpenStreetMap: $osm"
            },
            onError = {
                viewBinding.textViewTestOpenCellIdResponse.text =
                    "Paras: $paras\n\nOpenCelliD: ${it.url}\n\nError: ${it.error}"
            },
        )
    }

    companion object {
        private val TAG = OpenCellIdActivity::class.simpleName
    }
}
