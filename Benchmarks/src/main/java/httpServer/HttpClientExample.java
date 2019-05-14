package httpServer;

import java.util.Map;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientExample {

    final CloseableHttpClient httpclient = HttpClients.createDefault();
    final Gson gson = new Gson();

    // HTTP POST request
    public CloseableHttpResponse sendPost(Map<String, Object> entity) throws Exception {
        HttpPost httpPost = new HttpPost("<URL>");
        String json = gson.toJson(entity);
        // add header
        httpPost.setHeader("Authorization", "<Password>");
        httpPost.setHeader("Content-type", "application/json;charset=UTF-8");

        StringEntity requestEntity = new StringEntity(
                json,
                ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);

        return httpclient.execute(httpPost);
    }

    public CloseableHttpResponse sendGet(long id) throws Exception {
        HttpGet httpGet = new HttpGet(String.format("<URL>", id));
        // add header
        httpGet.setHeader("Authorization", "<Password>");

        return httpclient.execute(httpGet);
    }

    public CloseableHttpResponse sendCount() throws Exception {
        HttpGet httpGet = new HttpGet("<URL>");
        // add header
        httpGet.setHeader("Authorization", "<Password>");

        return httpclient.execute(httpGet);
    }

}
