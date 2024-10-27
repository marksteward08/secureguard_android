package com.nud.secureguardtech.logic.command.helper

import android.util.Log
import com.nud.secureguardtech.R
import com.nud.secureguardtech.data.Settings
import com.nud.secureguardtech.logic.ComponentHandler
import com.nud.secureguardtech.net.OpenCelliDRepository
import com.nud.secureguardtech.net.OpenCelliDSpec
import com.nud.secureguardtech.utils.CellParameters


class Cell {

    companion object {
        private val TAG = Cell::class.simpleName

        fun sendGSMCellLocation(ch: ComponentHandler) {
            val context = ch.context

            val apiAccessToken = ch.settings.get(Settings.SET_OPENCELLID_API_KEY) as String
            if (apiAccessToken.isEmpty()) {
                Log.i(TAG, "Cannot send cell location: Missing API Access Token")
                return
            }

            val paras = CellParameters.queryCellParametersFromTelephonyManager(context)
            if (paras == null) {
                Log.i(TAG, "No cell location found")
                ch.sender.sendNow(context.getString(R.string.OpenCellId_test_no_connection))
                return
            }
            ch.sender.sendNow(paras.prettyPrint())

            val repo = OpenCelliDRepository.getInstance(OpenCelliDSpec(context))

            repo.getCellLocation(
                paras, apiAccessToken,
                onSuccess = {
                    ch.locationHandler.newLocation("OpenCelliD", it.lat, it.lon)
                },
                onError = {
                    val string = ch.context.getString(R.string.JSON_RL_Error)
                    ch.sender.sendNow(string + it.url)
                },
            )
        }
    }
}
