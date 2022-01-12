package hu.minhiriathaen.oqcp.test.util;

public class AtlassianHostBuilder {

  private transient String clientKey = AtlassianUtil.CLIENT_KEY;

  private transient String publicKey = AtlassianUtil.PUBLIC_KEY;

  private transient String oauthClientId = "some-client-id";

  private transient String sharedSecret = AtlassianUtil.SHARED_SECRET;

  private transient String baseUrl = AtlassianUtil.BASE_URL;

  private transient String productType = AtlassianUtil.PRODUCT_TYPE;

  private transient String description = "Test host";

  private transient String serviceEntitlementNumber = null;

  private transient boolean addonInstalled = true;

  public AtlassianHostBuilder withClientKey(final String clientKey) {
    this.clientKey = clientKey;
    return this;
  }

  public AtlassianHostBuilder withPublicKey(final String publicKey) {
    this.publicKey = publicKey;
    return this;
  }

  public AtlassianHostBuilder withOauthClientId(final String oauthClientId) {
    this.oauthClientId = oauthClientId;
    return this;
  }

  public AtlassianHostBuilder withSharedSecret(final String sharedSecret) {
    this.sharedSecret = sharedSecret;
    return this;
  }

  public AtlassianHostBuilder withBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public AtlassianHostBuilder withProductType(final String productType) {
    this.productType = productType;
    return this;
  }

  public AtlassianHostBuilder withDescription(final String description) {
    this.description = description;
    return this;
  }

  public AtlassianHostBuilder withServiceEntitlementNumber(final String serviceEntitlementNumber) {
    this.serviceEntitlementNumber = serviceEntitlementNumber;
    return this;
  }

  public AtlassianHostBuilder withAddonInstalled(final boolean addonInstalled) {
    this.addonInstalled = addonInstalled;
    return this;
  }

  /**
   * Builds a new AtlassianHost instance with the set parameters.
   *
   * @return new AtlassianHost instance
   */
  public com.atlassian.connect.spring.AtlassianHost build() {
    final com.atlassian.connect.spring.AtlassianHost host =
        new com.atlassian.connect.spring.AtlassianHost();
    host.setClientKey(clientKey);
    host.setPublicKey(publicKey);
    host.setOauthClientId(oauthClientId);
    host.setSharedSecret(sharedSecret);
    host.setBaseUrl(baseUrl);
    host.setProductType(productType);
    host.setDescription(description);
    host.setServiceEntitlementNumber(serviceEntitlementNumber);
    host.setAddonInstalled(addonInstalled);
    return host;
  }
}
