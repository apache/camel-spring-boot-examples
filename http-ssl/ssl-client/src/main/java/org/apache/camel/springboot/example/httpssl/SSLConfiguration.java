package org.apache.camel.springboot.example.httpssl;

import org.apache.camel.support.jsse.FilterParameters;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextClientParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SSLConfiguration {

	@Bean("clientConfigOneWay")
	@Profile("oneway")
	public SSLContextParameters sslContextParameters(@Value("${keystore-password}") final String password) {
		return getTrustedSSLContextParameters(password);
	}

	@Bean("clientConfigTwoWays")
	@Profile("twoways")
	public SSLContextParameters sslContextParametersTwoWays(@Value("${keystore-password}") final String password) {
		final SSLContextParameters sslContextParameters = getTrustedSSLContextParameters(password);

		final KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource("classpath:client.jks");
		ksp.setPassword(password);

		final KeyManagersParameters kmp = new KeyManagersParameters();
		kmp.setKeyStore(ksp);
		kmp.setKeyPassword(password);

		sslContextParameters.setKeyManagers(kmp);
		sslContextParameters.setCertAlias("client");

		return sslContextParameters;
	}

	private SSLContextParameters getTrustedSSLContextParameters(final String password) {
		final SSLContextParameters sslContextParameters = new SSLContextParameters();

		final KeyStoreParameters ksp = new KeyStoreParameters();
		ksp.setResource("classpath:client-truststore.jks");
		ksp.setPassword(password);

		final FilterParameters filter = new FilterParameters();
		filter.getInclude().add(".*");

		final SSLContextClientParameters sccp = new SSLContextClientParameters();
		sccp.setCipherSuitesFilter(filter);

		final TrustManagersParameters tmp = new TrustManagersParameters();
		tmp.setKeyStore(ksp);

		sslContextParameters.setTrustManagers(tmp);
		sslContextParameters.setClientParameters(sccp);

		return sslContextParameters;
	}
}
