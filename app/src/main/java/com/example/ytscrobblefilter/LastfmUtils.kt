package com.example.ytscrobblefilter

import android.util.Log
import com.google.gson.Gson
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.internal.cache.CacheStrategy
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.TreeMap
import java.util.regex.Pattern


class LastfmUtils {

    public val apiKey = BuildConfig.LFMapikey
    private val apiSecret = BuildConfig.LFMSecret
    private val apiRootUrl = "https://ws.audioscrobbler.com/2.0/"

    private var client: OkHttpClient? = OkHttpClient()
    private var gson: Gson? = Gson()

    suspend fun getArtistInfo(artistName: String) {
        /*
        val url = BASE_URL
        val requestBody = "method=track.scrobble&artist=$artist&track=$track&sk=$sessionKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()

        withContext(Dispatchers.IO) {
            val request = Builder()
                .url(url)
                .post(create(MediaType.parse("application/x-www-form-urlencoded"), requestBody))
                .build()
        }

        consumer.sign(request)
        val response: Response = client.newCall(request).execute()
        // Handle the response as needed
        */

    }

    private fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun sendGetRequest(url: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    /*
    fun getMobileSession(apiRoot: String?, username: String, password: String): Session? {

        var password = password

        //check if password is MD5
        val MD5_PATTERN = Pattern.compile("[a-fA-F0-9]{32}");
        if (!(password.length == 32 && MD5_PATTERN.matcher(password).matches())){
            password = md5(password)
        }
        //

        val authToken: String = md5(username + password)
        val params: TreeMap<String?, String?> = TreeMap(mapOf("api_key" to apiKey, "username" to username, "authToken" to authToken))


        val sig: String = createSignature("auth.getMobileSession", params, apiSecret)


        query = apiRootUrl + "auth.getMobileSession" +

        val result = call(
            "auth.getMobileSession",
            apiKey,
            "username",
            username,
            "authToken",
            authToken,
            "api_sig",
            sig
        )

        val element: DomElement = result.getContentElement()
        return Session.sessionFromElement(apiRoot, element, apiKey, secret)
    }

    private fun createSignature(method: String?, params: TreeMap<String?, String?>, secret: String?): String {

        params["method"] = method
        val b = StringBuilder(100)
        for ((key, value) in params) {
            b.append(key)
            b.append(value)
        }
        b.append(secret)
        return md5(b.toString())
    }

    fun call(method: String, apiKey: String?, params: MutableMap<String?, String?>): Result? {

        var inputStream: InputStream? = null
        // fill parameter map with apiKey and session info

        var response: Response? = null

        return try {
            response = getOkHttpResponse(apiRootUrl, method, params, null, false)

            val responseBody: ResponseBody = response.body()
            inputStream = responseBody.byteStream()

            val result: Result = createResultFromInputStream(inputStream)

            if (!result.isSuccessful()) {
                val errMsg = java.lang.String.format("$method failed with result: %s%n", result)
                Log.w("Call error", errMsg)
            }

            result
        } catch (e: Exception) {
            throw CallException(e)
        } finally {
            if (response != null) {
                response.close()
            }
        }
    }

    private fun getOkHttpResponse(apiRootUrl: String, method: String, params: Map<String, String>, cacheStrategy: CacheStrategy?, isTlsNoVerify: Boolean): Response? {

        val query: String = buildPostBody(method, params)


        /*
        "auth.getMobileSession",
                    apiKey,
                    "username",
                    username,
                    "authToken",
                    authToken,
                    "api_sig",
                    sig
         */

        /*
            password (Required) : The user's password in plaintext.
            username (Required) : The user's Last.fm username.
            api_key (Required) : A Last.fm API key.
            api_sig (Required) : A Last.fm method signature. See Section 8 for more information.
         */

        val requestBuilder: Request.Builder = Builder()
        if (method.contains(".get")) {
            requestBuilder.url("$apiRootUrl?$query")
        } else {
            requestBuilder.url(apiRootUrl)
                .post(create(query, MediaType.parse("application/x-www-form-urlencoded")))
        }
        if (cacheStrategy == null) cacheStrategy = CACHE_FIRST
        when (cacheStrategy) {
            CACHE_FIRST -> {}
            CACHE_ONLY_INCLUDE_EXPIRED -> requestBuilder.cacheControl(CacheControl.FORCE_CACHE)
            NETWORK_ONLY -> requestBuilder.cacheControl(CacheControl.FORCE_NETWORK)
            CACHE_FIRST_ONE_DAY -> requestBuilder.cacheControl(
                Builder()
                    .maxAge(1, TimeUnit.DAYS)
                    .build()
            )

            CACHE_FIRST_ONE_WEEK -> requestBuilder.cacheControl(
                Builder()
                    .maxAge(7, TimeUnit.DAYS)
                    .build()
            )
        }

        return client?.newCall(requestBuilder.build())?.execute()
    }
    */

}

