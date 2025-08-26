package com.yooshyasha.factcheckerpet.services

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.customsearch.v1.CustomSearchAPI
import com.google.api.services.customsearch.v1.CustomSearchAPIRequestInitializer
import com.google.api.services.customsearch.v1.model.Result
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GoogleApiService {
    @Value("\${google.api.key}")
    private lateinit var googleApiKey: String

    @Value("\${google.custom.search.cx}")
    private lateinit var customSearchCx: String

    fun search(query: String): String {
        return try {
            val customsearch = CustomSearchAPI.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                null
            )
                .setGoogleClientRequestInitializer(CustomSearchAPIRequestInitializer(googleApiKey))
                .build()

            val listRequest = customsearch.cse().list()
            listRequest.q = query
            listRequest.cx = customSearchCx
            listRequest.num = 50

            val searchResult = listRequest.execute()
            formatResults(searchResult.items)
        } catch (e: Exception) {
            "Ошибка поиска: ${e.message}"
        }
    }

    private fun formatResults(results: List<Result>?): String {
        if (results.isNullOrEmpty()) {
            return "Результаты не найдены"
        }

        val output = StringBuilder()
        results.forEachIndexed { index, result ->
            output.append("${index + 1}. ${result.title}\n")
            output.append("URL: ${result.link}\n")
            output.append("${result.snippet}\n\n")
        }
        return output.toString()
    }
}