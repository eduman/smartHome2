package it.eduman.mobileHome2.communication;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import it.eduman.android.commons.utilities.KeyValue;

public class WebServicesWrapper {

	private String namespace = "";
	private String webServiceUrl = "";
	
	public WebServicesWrapper (String namespace, String webServiceUrl){
		this.namespace = namespace;
		this.webServiceUrl = webServiceUrl;
	}
	
	public String callWebServiceMethod(String methodName, List<KeyValue> parameters) throws IOException, XmlPullParserException {
			String soapAction = ""; // Because my WSDL has empty soapActions for all methods.
			SoapObject request = new SoapObject(this.namespace, methodName);
			if (parameters != null) {
				for (KeyValue parameter : parameters) {
					request.addProperty(parameter.getKey(), parameter.getValue());
				}
			}
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			String url = this.webServiceUrl;
			HttpTransportSE httpTransport = new HttpTransportSE(url);
			httpTransport.call(soapAction, envelope);
			Object result = envelope.getResponse();
			return result.toString();
	}		
}
