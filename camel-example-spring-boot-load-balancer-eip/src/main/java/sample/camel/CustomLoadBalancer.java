package sample.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.processor.loadbalancer.LoadBalancerSupport;

public class CustomLoadBalancer  extends LoadBalancerSupport {
	@Override
	public boolean process(Exchange exchange, AsyncCallback callback) {
		String body = exchange.getIn().getBody(String.class);
		try {
			if ("AE".contains(body)){
				getProcessors().get(0).process(exchange);
			} else if ("BCD".contains(body))
				getProcessors().get(1).process(exchange);
		}
		catch (Exception e) {
			exchange.setException(e);
		}
		callback.done(true);
		return true;
	}
}