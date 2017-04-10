package com.gu.tip

import okhttp3.{MediaType, OkHttpClient, Request, RequestBody}

trait HttpClientIf {
  def get(endpoint: String, authHeader: (String, String)): (Int, String)
  def post(endpoint: String, authHeader: (String, String), jsonBody: String): (Int, String)
}

trait HttpClient extends HttpClientIf {
  override def get(endpoint: String, authHeader: (String, String)): (Int, String) = {
    val request = new Request.Builder()
      .url(endpoint)
      .addHeader(authHeader._1, authHeader._2)
      .build()
    val response = client.newCall(request).execute()
    (response.code(), response.body().string)
  }

  override def post(endpoint: String, authHeader: (String, String), jsonBody: String): (Int, String) = {
    val request = new Request.Builder()
      .url(endpoint)
      .addHeader(authHeader._1, authHeader._2)
      .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
      .build()

    val response = client.newCall(request).execute()
    (response.code(), response.body().string())
  }

  private val client = new OkHttpClient()
}
