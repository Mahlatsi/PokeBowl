package com.pokebowl.pokeapiandroid.data.repositories

import com.pokebowl.pokeapiandroid.utils.NetworkResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

open class BaseRepository {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResource<T> = withContext(Dispatchers.IO) {
        try {
            NetworkResource.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    NetworkResource.Failure(
                        false,
                        throwable.code(),
                        throwable.response()?.errorBody()
                    )
                }
                else -> {
                    NetworkResource.Failure(true, null, null)
                }
            }
        }
    }
}