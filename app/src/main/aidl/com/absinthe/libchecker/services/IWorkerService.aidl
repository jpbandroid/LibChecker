package com.absinthe.libchecker.services;
import com.absinthe.libchecker.services.OnWorkerListener;

interface IWorkerService {
    void initKotlinUsage();
    void registerOnWorkerListener(in OnWorkerListener listener);
    void unregisterOnWorkerListener(in OnWorkerListener listener);
}