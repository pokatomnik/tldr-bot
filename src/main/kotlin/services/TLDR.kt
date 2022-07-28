package services

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

enum class Platform {
    android,
    common,
    linux,
    osx,
    sunos,
    windows,
}

internal interface TLDRRetrofitService {
    @GET("/tldr-pages/tldr/main/pages/{platform}/{command}.md")
    suspend fun fetchTLDR(@Path("platform") platform: Platform, @Path("command") command: String): ResponseBody
}

class TLDRService {
    private val retrofitService = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com")
        .build()

    private val tldrClient = retrofitService.create(TLDRRetrofitService::class.java)

    suspend fun fetchTLDR(command: String): String? {
        for (platform in Platform.values()) {
            try {
                return tldrClient.fetchTLDR(platform, command).string()
            } catch (e: Throwable) {
                continue
            }
        }
        return null
    }
}