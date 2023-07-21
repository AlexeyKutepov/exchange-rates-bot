package ru.akutepov.exchangeratesbot.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ru.akutepov.exchangeratesbot.client.CbrClient;
import ru.akutepov.exchangeratesbot.exception.ServiceException;
import ru.akutepov.exchangeratesbot.service.ExchangeRatesService;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesServiceImpl.class);

    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";

    @Autowired
    private CbrClient client;

    @Cacheable(value = "usd", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getUSDExchangeRate() throws ServiceException {
        var xmlOptional = client.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, USD_XPATH);
    }

    @Cacheable(value = "eur", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getEURExchangeRate() throws ServiceException {
        var xmlOptional = client.getCurrencyRatesXML();
        String xml = xmlOptional.orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return extractCurrencyValueFromXML(xml, EUR_XPATH);
    }

    @CacheEvict("usd")
    @Override
    public void clearUSDCache() {
        LOG.info("Cache \"usd\" cleared!");
    }

    @CacheEvict("eur")
    @Override
    public void clearEURCache() {
        LOG.info("Cache \"eur\" cleared!");
    }

    private static String extractCurrencyValueFromXML(String xml, String xpathExpression)
            throws ServiceException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);

            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new ServiceException("Не удалось распарсить XML", e);
        }
    }
}
