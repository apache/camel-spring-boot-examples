package sample.camel;

import org.apache.camel.builder.RouteBuilder;

import org.springframework.stereotype.Component;

@Component
public class MyCamelTestRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("direct:start")
				.to("fhir://read/resourceById?resourceClass=Patient&stringId=1&serverUrl={{serverUrl}}&fhirVersion={{fhirVersion}}")
				.to("mock:result");
	}
}
