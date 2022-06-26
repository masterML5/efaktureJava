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

import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.commons.codec.binary.Base64;

 /*
 * @author Milos Jelic
 */
public class HttpGetIzlazni {

    private static String htmlIzlazni2;
    private static String salesId;
    private static String komentarStatusa;
    private static Integer prikazStorno;
    private static String stornoKomentar;
    private static String statusDokumenta;
    private static String uspesnoSkidanje;
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
        Request requestSviIds = Request.Post(apiUrlSviIzlazniIds);
        requestSviIds.setHeader("Accept", "text/plain");
        requestSviIds.setHeader("Apikey", apiKey);
       
        HttpResponse httpResponse = requestSviIds.execute().returnResponse();
    
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
             Request requestIzlazniPoId = Request.Get(url);
             requestIzlazniPoId.setHeader("Accept", "*/*");
             requestIzlazniPoId.setHeader("Apikey", apiKey);
             HttpResponse httpResponseIzlazni = requestIzlazniPoId.execute().returnResponse();

             if (httpResponseIzlazni.getEntity() != null) {
             String htmlIzlazni = EntityUtils.toString(httpResponseIzlazni.getEntity());
  
             JSONObject obj2 = new JSONObject(htmlIzlazni);  
             statusDokumenta = obj2.getString("Status"); 
             if("Cancelled".equals(statusDokumenta)){
              prikazStorno = 1;
              stornoKomentar = obj2.getString("StornoComment");
                 
             }else{
              prikazStorno = 0;
             }
            
             //provera da li postoji komentar
             if(obj2.isNull("Comment")){
              komentarStatusa = "Nema komentara";
             }else{
              komentarStatusa = obj2.getString("Comment");
             }
             
             
             //Preuzimanje razlicitih polja iz XML fajla izlaznog dokumenta
             String urlXML = apiUrlIzlazniXMLUBL + salesId;
             Request requestIzlazniXML = Request.Get(urlXML);
             requestIzlazniXML.setHeader("Accept", "*/*");
             requestIzlazniXML.setHeader("Apikey", apiKey);
             HttpResponse httpResponseIzlazni2 = requestIzlazniXML.execute().returnResponse();
            
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
                
                
                //ispisivanje
                System.out.printf("Pib kupca : %s%n", uid);
                System.out.printf("Kupac : %s%n", fname);
                

            }

        }
             String iznos = doc.getElementsByTagName("cbc:PayableAmount").item(0).getTextContent();
             String brojdok = doc.getElementsByTagName("cbc:ID").item(0).getTextContent();
             String valuta  = doc.getElementsByTagName("cbc:DocumentCurrencyCode").item(0).getTextContent();
             //skidanje pdf-a sa sefa
             skiniPdf(doc,brojdok);

             
              //ispisivanje
             
             System.out.println("Iznos : " + iznos + " " + valuta);
             System.out.println("Broj dokumenta : " + brojdok);
             System.out.println("Status : " + getStatusSrb(statusDokumenta));
             if(prikazStorno == 1){
             System.out.println("Storno komentar :" + stornoKomentar);  
             }
             System.out.println("Komentar statusa : " + komentarStatusa);
             System.out.println("InvoiceID : " + salesId);
             System.out.println("PDF Dokument : " + uspesnoSkidanje);
             System.out.println("=========================================================");
         
         
           }
    
}
        
    }
    }
 
}
     private static String skiniPdf(Document doc, String brojdok) throws FileNotFoundException, IOException{
        NodeList nList2 = doc.getElementsByTagName("env:DocumentHeader");
              for (int k = 0; k < nList2.getLength(); k++) {
              Node nNode2 = nList2.item(k);
            if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nNode2;
                Node nodePdf = elem.getElementsByTagName("env:DocumentPdf").item(0);
                String pdf = nodePdf.getTextContent();
                byte[] decoded = java.util.Base64.getDecoder().decode(pdf);
                //definisati gde ce se fakture smestati! PDF je cca 65KB
                  try (FileOutputStream fosPdf = new FileOutputStream("e:/efakture/eFakturePDF_"+brojdok+".pdf")) {
                      fosPdf.write(decoded);
                      fosPdf.flush();
                       uspesnoSkidanje = "Uspesno ste skinuli pdf dokumenta "+brojdok;
                              
                  }
            }

        }

         return uspesnoSkidanje;
     
     }
     private static String getStatusSrb(String str) {
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
