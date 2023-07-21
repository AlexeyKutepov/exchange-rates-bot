package ru.akutepov.exchangeratesbot.service;

import ru.akutepov.exchangeratesbot.exception.ServiceException;

public interface ExchangeRatesService {

    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;

    void clearUSDCache();

    void clearEURCache();

}
