import org.apache.commons.codec.binary.Base64;
import java.lang.*;
    
public class Test {
    public static void main(String[] args){
    // initializes api key, httpclient and http post, as well as post params
        String api_key = "AIzaSyAPPm6FmMfhXHxKoScqLuRcD-9H3QSm8f4";
        HttpClient httpclient;
        HttpPost httppost;
        ArrayList<NameValuePair> postParameters;

        // init httpclient and httppost to correct URL
        httpclient = new DefaultHttpClient();
        httppost = new HttpPost("https://vision.googleapis.com/v1/images:annotate?key="+api_key);

        // File f = new File("test.png");
        // String encodestring = ImportPhoto(f);
        // System.out.println(encodestring);
        
        // This is a list of filenames as strings which the app will get when it gets the top whatever numebr files
        String[] filenames = new String[]{"test.png", "Scan 2.jpeg"};
        
        //need to encode each file
        for (int i = 0; i < filenames.size(); i++) {
            // encoded image
            String encoded = ImportPhoto(filenames[i]);

            // JSON version of data that does the request
            String json = "{\"requests\": [{\"image\": {\"content\": \"abc\"}, \"features\": [{\"type\": \"FACE_DETECTION\", \"maxResults\": 10}]}]}";
            JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
            
            // reinit params as blank resizeable Array and adds the json as the data param
            postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("data", data));

            // httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            // HttpResponse response = httpclient.execute(httpPost);
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
                HttpResponse response = httpclient.execute(httpPost);
                // Print out the response message
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                System.out.println("sad" + e.getMessage());
            } 
        }  
    }

    // encodes file to base64
    public static String ImportPhoto(File file){
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] imageData = new byte[(int)file.length()];
        fileInputStreamReader.read(imageData);
        encodedfile = new String(Base64.encodeBase64(bytes),"UTF-8");
        return encodedfile;
    }
}


