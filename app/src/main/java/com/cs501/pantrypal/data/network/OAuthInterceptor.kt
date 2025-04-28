package com.cs501.pantrypal.data.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class OAuthInterceptor(private val consumerKey: String, private val consumerSecret: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val allParams = collectAllParams(originalRequest)

        val signature = generateSignature(originalRequest, allParams)

        val signedRequest = buildSignedRequest(originalRequest, allParams, signature)

        return chain.proceed(signedRequest)
    }

    private fun collectAllParams(request: Request): Map<String, String> {
        val params = mutableMapOf<String, String>()

        val url = request.url
        for (i in 0 until url.querySize) {
            val key = url.queryParameterName(i)
            val value = url.queryParameterValue(i) ?: ""
            params[key] = value
        }

        params["oauth_consumer_key"] = consumerKey
        params["oauth_signature_method"] = "HMAC-SHA1"
        params["oauth_timestamp"] = (System.currentTimeMillis() / 1000).toString()
        params["oauth_nonce"] = generateNonce()
        params["oauth_version"] = "1.0"

        return params
    }

    private fun generateSignature(request: Request, params: Map<String, String>): String {
        val method = request.method.uppercase()
        val baseUrl = request.url.newBuilder().query(null).build().toString()

        val sortedParams = params.entries
            .sortedWith(compareBy({ it.key }, { it.value }))
            .joinToString("&") {
                "${encode(it.key)}=${encode(it.value)}"
            }

        val baseString = "${method}&${encode(baseUrl)}&${encode(sortedParams)}"

        val signingKey = "${encode(consumerSecret)}&"

        val mac = Mac.getInstance("HmacSHA1")
        val secretKeySpec = SecretKeySpec(signingKey.toByteArray(Charsets.UTF_8), "HmacSHA1")
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(baseString.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(rawHmac, Base64.NO_WRAP)
    }

    private fun buildSignedRequest(request: Request, params: Map<String, String>, signature: String): Request {
        val originalUrl = request.url
        val urlBuilder = originalUrl.newBuilder()

        urlBuilder.addQueryParameter("oauth_consumer_key", params["oauth_consumer_key"])
        urlBuilder.addQueryParameter("oauth_signature_method", params["oauth_signature_method"])
        urlBuilder.addQueryParameter("oauth_timestamp", params["oauth_timestamp"])
        urlBuilder.addQueryParameter("oauth_nonce", params["oauth_nonce"])
        urlBuilder.addQueryParameter("oauth_version", params["oauth_version"])
        urlBuilder.addQueryParameter("oauth_signature", signature)

        return request.newBuilder()
            .url(urlBuilder.build())
            .build()
    }

    private fun generateNonce(): String {
        val random = SecureRandom()
        val nonce = ByteArray(16)
        random.nextBytes(nonce)
        return nonce.joinToString("") { "%02x".format(it) }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }
}
