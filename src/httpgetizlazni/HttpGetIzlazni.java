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

/**
 *
 * @author Milos Jelic
 */
public class HttpGetIzlazni {

    private static String htmlIzlazni2;
    private static String SalesId;
    /**
     * @param args the command line arguments
     */
     
    public static void main(String[] args) throws IOException, JSONException, ParserConfigurationException, SAXException {
        
        Request request = Request.Post("https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice/ids");
      //  String body = "\"";
     //   request.bodyString(body,ContentType.APPLICATION_FORM_URLENCODED);
        request.setHeader("Accept", "text/plain");
        request.setHeader("Apikey", "a9a8a077-fd40-4140-a276-e2efd35e50b7");
       
        HttpResponse httpResponse = request.execute().returnResponse();
     //   System.out.println(httpResponse.getStatusLine());
        if (httpResponse.getEntity() != null) {
	String html = EntityUtils.toString(httpResponse.getEntity());
//	System.out.println(html);
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
  
      //  System.out.println("Each element of ArrayList");  
        for(int i=0; i<listdata.size(); i++) {  
            //Printing each element of ArrayList
            
             SalesId = listdata.get(i).toString();
             String url = "https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice?invoiceId=" + SalesId;
             Request requestIzlazni = Request.Get(url);
             requestIzlazni.setHeader("Accept", "*/*");
             requestIzlazni.setHeader("Apikey", "a9a8a077-fd40-4140-a276-e2efd35e50b7");
             HttpResponse httpResponseIzlazni = requestIzlazni.execute().returnResponse();
            // System.out.println(httpResponseIzlazni.getStatusLine());
             if (httpResponseIzlazni.getEntity() != null) {
             String htmlIzlazni = EntityUtils.toString(httpResponseIzlazni.getEntity());
            // System.out.println(htmlIzlazni);
             JSONObject obj2 = new JSONObject(htmlIzlazni);  
             String statusDokumenta = obj2.getString("Status");    
             

             String url2 = "https://demoefaktura.mfin.gov.rs/api/publicApi/sales-invoice/xml?invoiceId=" + SalesId;
             Request requestIzlazni2 = Request.Get(url2);
             requestIzlazni2.setHeader("Accept", "*/*");
             requestIzlazni2.setHeader("Apikey", "a9a8a077-fd40-4140-a276-e2efd35e50b7");
             HttpResponse httpResponseIzlazni2 = requestIzlazni2.execute().returnResponse();
             //System.out.println(httpResponseIzlazni2.getStatusLine().getStatusCode());
             if (httpResponseIzlazni2.getStatusLine().getStatusCode() != 200) {
            // System.out.println("Faktura " + SalesId + " Nema UBL FILE");
             }else{
             htmlIzlazni2 = EntityUtils.toString(httpResponseIzlazni2.getEntity());
             DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             InputSource src = new InputSource();
             src.setCharacterStream(new StringReader(htmlIzlazni2));

             Document doc = builder.parse(src);
             
             //String kupac = doc.getElementsByTagName("cac:AccountingCustomerParty").item(0);
             
             doc.getDocumentElement().normalize();

            // System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
             NodeList nList = doc.getElementsByTagName("cac:AccountingCustomerParty");
           //  System.out.println(nList);
             for (int j = 0; j < nList.getLength(); j++) {

            Node nNode = nList.item(j);

          //  System.out.println("\nCurrent Element: " + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element elem = (Element) nNode;

                Node nodelId = elem.getElementsByTagName("cbc:EndpointID").item(0);
                String uid = nodelId.getTextContent();

                Node node1 = elem.getElementsByTagName("cbc:Name").item(0);
                String fname = node1.getTextContent();

//                Node node2 = elem.getElementsByTagName("lastname").item(0);
//                String lname = node2.getTextContent();
//
//                Node node3 = elem.getElementsByTagName("occupation").item(0);
//                String occup = node3.getTextContent();

                System.out.printf("Pib kupca : %s%n", uid);
                System.out.printf("Kupac : %s%n", fname);
//                System.out.printf("Last name: %s%n", lname);
//                System.out.printf("Occupation: %s%n", occup);
            }

        }
           //  System.out.println(kupac);
           
             String iznos = doc.getElementsByTagName("cbc:PayableAmount").item(0).getTextContent();
             String brojdok = doc.getElementsByTagName("cbc:ID").item(0).getTextContent();
             String valuta  = doc.getElementsByTagName("cbc:DocumentCurrencyCode").item(0).getTextContent();
             

             System.out.println("Iznos : " + iznos + " " + valuta);
             System.out.println("Broj dokumenta : " + brojdok);
             System.out.println("Status : " + getStatusSrb(statusDokumenta));
             System.out.println("=========================================================");
         
           //  String name = doc.getElementsByTagName("name").item(0).getTextContent();
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
    }
    return status;
    }
   
}
