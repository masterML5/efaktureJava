/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpgetizlazni;


import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;

 /*
 * @author Milos Jelic
 */
public class HttpGetIzlazni {

    private static String htmlIzlazni2;
    private static String salesId;
    private static final String apiUrlSviIzlazniIds = "https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice/ids";
    private static final String apiUrlSviIzlazni = "https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice?invoiceId=";
    private static final String apiUrlIzlazniXMLUBL = "https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice/xml?invoiceId=";
    private static final String apiKey = "a9a8a077-fd40-4140-a276-e2efd35e50b7";
/**
   
    /**
     * @param args the command line arguments
     */
     
    public static void main(String[] args) throws IOException, JSONException, ParserConfigurationException, SAXException {
        // Preuzimanje svih Izlaznih ID-a
        Request request = Request.Post(apiUrlSviIzlazniIds);
        request.setHeader("Accept", "text/plain");
        request.setHeader("Apikey", apiKey);
       
        HttpResponse httpResponse = request.execute().returnResponse();
    
        if (httpResponse.getEntity() != null) {
	String html = EntityUtils.toString(httpResponse.getEntity());

        JSONObject obj = new JSONObject(html);  
        JSONArray salesIds;
        //getting values form the JSONObject
        salesIds = obj.getJSONArray("SalesInvoiceIds");
        ArrayList<Object> listdata = new ArrayList<Object>();  
          
        //Checking whether the JSON array has some value or not  
        if (salesIds != null) {   
              
            //Iterating JSON array  
            for (int i=0;i<salesIds.length();i++){   
                  
                //Adding each element of JSON array into ArrayList  
                listdata.add(salesIds.get(i));  
            }   
        }  
        //Iterating ArrayList to print each element  
  
 
        for(int i=0; i<listdata.size(); i++) {  
            //Printing each element of ArrayList
            //Svi izlazni dokumenti po ID-u (salesId)
             salesId = listdata.get(i).toString();
             String url =  apiUrlSviIzlazni + salesId;
             Request requestIzlazni = Request.Get(url);
             requestIzlazni.setHeader("Accept", "*/*");
             requestIzlazni.setHeader("Apikey", apiKey);
             HttpResponse httpResponseIzlazni = requestIzlazni.execute().returnResponse();

             if (httpResponseIzlazni.getEntity() != null) {
             String htmlIzlazni = EntityUtils.toString(httpResponseIzlazni.getEntity());
  
             JSONObject obj2 = new JSONObject(htmlIzlazni);  
             String statusDokumenta = obj2.getString("Status");    
             
             //Preuzimanje razlicitih polja iz XML fajla izlaznog dokumenta
             String urlXML = apiUrlIzlazniXMLUBL + salesId;
             Request requestIzlazni2 = Request.Get(urlXML);
             requestIzlazni2.setHeader("Accept", "*/*");
             requestIzlazni2.setHeader("Apikey", apiKey);
             HttpResponse httpResponseIzlazni2 = requestIzlazni2.execute().returnResponse();
            
             if (httpResponseIzlazni2.getStatusLine().getStatusCode() != 200) {
             }else{
             htmlIzlazni2 = EntityUtils.toString(httpResponseIzlazni2.getEntity());
             DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             InputSource src = new InputSource();
             src.setCharacterStream(new StringReader(htmlIzlazni2));

             Document doc = builder.parse(src);
             doc.getDocumentElement().normalize();
             NodeList nList = doc.getElementsByTagName("cac:AccountingCustomerParty");
             for (int j = 0; j < nList.getLength(); j++) {

            Node nNode = nList.item(j);


            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element elem = (Element) nNode;

                Node nodelId = elem.getElementsByTagName("cbc:EndpointID").item(0);
                String uid = nodelId.getTextContent();

                Node node1 = elem.getElementsByTagName("cbc:Name").item(0);
                String fname = node1.getTextContent();

                System.out.printf("Pib kupca : %s%n", uid);
                System.out.printf("Kupac : %s%n", fname);

            }

        }
  
           
             String iznos = doc.getElementsByTagName("cbc:PayableAmount").item(0).getTextContent();
             String brojdok = doc.getElementsByTagName("cbc:ID").item(0).getTextContent();
             String valuta  = doc.getElementsByTagName("cbc:DocumentCurrencyCode").item(0).getTextContent();
             

             System.out.println("Iznos : " + iznos + " " + valuta);
             System.out.println("Broj dokumenta : " + brojdok);
             System.out.println("Status : " + getStatusSrb(statusDokumenta));
             System.out.println("InvoiceID : " + salesId);
             System.out.println("=========================================================");
         
         
           }
    
}
        
    }
    }
 
}
     private static String  getStatusSrb(String str) {
        String status = null;
       switch(str){
        case "Rejected":
            status = "Odbijen";
            break;
        case "Approved":
            status = "Odobren";
            break;
        case "Sent":
            status = "Poslat";
            break;
        case "Cancelled":
            status = "Otkazana";
            break;
        case "Storno":
            status = "Storniran";
            break;
        case "Seen":
            status = "Pregledan";
            break;
        case "Draft":
            status = "Nacrt";
            break;
        default :
            status = str;
    }
    return status;
    }
   
}
