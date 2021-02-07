package com.jacekpietras.zoo.data.gateway

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jacekpietras.zoo.domain.gateway.MailGateway
import timber.log.Timber


class MailGatewayImpl(
    private val context: Context,
) : MailGateway {

    @Throws(MailGateway.NoMailClientFound::class)
    override fun sendMail(address: String, title: String, content: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("mailto:?subject=$title&body=$content&to=$address")
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Failed to send mail")
            throw MailGateway.NoMailClientFound()
        }
    }
}
