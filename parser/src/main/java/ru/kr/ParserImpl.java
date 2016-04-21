package ru.kr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserImpl implements Parser {
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String LOGIN_URL = "https://lk.megafon.ru/login/";
    private static final String DOLOGIN_URL = "https://lk.megafon.ru/dologin/";
    private static final String MEGAFON_URL = "https://lk.megafon.ru/";
    private static final String CSRF_K = "CSRF";
    private static final String LOGIN_K = "j_username";
    private static final String PSWD_K = "j_password";


    private List<String> cookies;
    private String sessionId;
    private HttpsURLConnection conn;
    private String login;
    private String pswd;


    public ParserImpl(String login, String pswd) {
        this.login = login;
        this.pswd = pswd;
    }

    public Double getMoney() throws Exception {

        CookieHandler.setDefault(new CookieManager());

        String page = getPageContent(LOGIN_URL);
        String postParams = getFormParams(page, login, pswd);

        // 2. Construct above post's content and then send a POST request for
        // authentication
        sendPost(DOLOGIN_URL, postParams);


        String result = getPageContent(MEGAFON_URL);
        System.out.println(result);
        String balanceClass = "private-office-td private-office-header-balans private-office-header-link-none";
        Document doc = Jsoup.parse(result);

        Element attrBalance = doc.getElementsByClass("private-office-header").get(0).child(0).child(3).child(0).child(1);
        String valBalance = attrBalance.html().toString();
//        System.out.println(valBalance);
//        System.out.println(valBalance.indexOf("<span"));
        valBalance = valBalance.substring(0, valBalance.indexOf("<span")-1);
        valBalance = valBalance.replace(",",".").trim();
        System.out.println(valBalance);
        Double money = Double.parseDouble(valBalance);
        return money;
//        System.out.println("MONEY="+money);
//        String balance = attrBalance.getElementsByClass("ui-label").get(0).html();
//        System.out.println(balance);



    }

    private void sendPost(String url, String postParams) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "dvp-connect.abcd.com");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "https://dvp-connect.abcd.com/cvpn");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + postParams);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // System.out.println(response.toString());

    }


    private String getPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpsURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));
        return response.toString();

    }

    public String getFormParams(String html, String username, String password)
            throws UnsupportedEncodingException, ParserConfigurationException {

        System.out.println("Extracting form's data...");
        Document doc = Jsoup.parse(html.toString());

        Elements forms = doc.getElementsByTag("form");
        Element form = forms.get(0);

        System.out.println("FORM");
        System.out.println(form);

        String csrfVal = form.getElementsByAttribute("name").get(0).attr("value");

        System.out.println("CSRF="+csrfVal);

        StringBuilder result = new StringBuilder();
        result.append(CSRF_K +"="+ URLEncoder.encode(csrfVal, "UTF-8")+"&");
        result.append(LOGIN_K +"="+ URLEncoder.encode(username, "UTF-8")+"&");
        result.append(PSWD_K+"="+URLEncoder.encode(password, "UTF-8"));
        return result.toString();
    }



    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public void setSessionId(String sessionId){
        this.sessionId = sessionId;
    }

}