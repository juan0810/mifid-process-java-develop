function fn() {
  var env = karate.env;
  karate.log('karate.env system property was:', env);
  
  if (!env) {
    env = 'dev';
  }
  
  var port = karate.properties['karate.port'] || '8081';
  var baseUrl = 'http://localhost:' + port + '/api/v1';
  
  var config = {
    env: env,
    baseUrl: baseUrl,
    correlationIdHeader: 'X-Correlation-ID',
    timeout: 10000,
    
    testData: {
      documentNumber: '00218852F',
      version: 1
    }
  };
  
  if (env === 'dev') {
    config.testData.documentNumber = '00218852F';
    config.testData.version = 1;
  } else if (env === 'uat') {
    config.testData.documentNumber = '00218852F';
    config.testData.version = 1;
  }
  
  karate.configure('connectTimeout', config.timeout);
  karate.configure('readTimeout', config.timeout);
  
  return config;
}