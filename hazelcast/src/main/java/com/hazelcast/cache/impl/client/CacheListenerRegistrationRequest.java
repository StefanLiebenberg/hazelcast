/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.cache.impl.client;

import com.hazelcast.cache.impl.CachePortableHook;
import com.hazelcast.cache.impl.ICacheService;
import com.hazelcast.cache.impl.operation.CacheListenerRegistrationOperation;
import com.hazelcast.client.impl.client.TargetClientRequest;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.security.permission.ActionConstants;
import com.hazelcast.security.permission.CachePermission;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.Operation;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import java.io.IOException;
import java.security.Permission;

/**
 * This client request  specifically calls {@link CacheListenerRegistrationOperation} on the server side.
 *
 * @see com.hazelcast.cache.impl.operation.CacheListenerRegistrationOperation
 */
public class CacheListenerRegistrationRequest
        extends TargetClientRequest {

    private String name;
    private CacheEntryListenerConfiguration cacheEntryListenerConfiguration;
    private boolean register;
    private Address target;

    public CacheListenerRegistrationRequest() {
    }

    public CacheListenerRegistrationRequest(String name, CacheEntryListenerConfiguration cacheEntryListenerConfiguration,
                                            boolean register, Address target) {
        this.name = name;
        this.cacheEntryListenerConfiguration = cacheEntryListenerConfiguration;
        this.register = register;
        this.target = target;
    }

    @Override
    public int getFactoryId() {
        return CachePortableHook.F_ID;
    }

    @Override
    public int getClassId() {
        return CachePortableHook.LISTENER_REGISTRATION;
    }

    @Override
    protected Operation prepareOperation() {
        return new CacheListenerRegistrationOperation(name, cacheEntryListenerConfiguration, register);
    }

    @Override
    protected InvocationBuilder getInvocationBuilder(Operation op) {
        return operationService.createInvocationBuilder(getServiceName(), op, target);
    }

    @Override
    public String getServiceName() {
        return ICacheService.SERVICE_NAME;
    }

    @Override
    public void write(PortableWriter writer)
            throws IOException {
        writer.writeUTF("n", name);
        writer.writeBoolean("r", register);
        ObjectDataOutput out = writer.getRawDataOutput();
        out.writeObject(cacheEntryListenerConfiguration);
        target.writeData(out);
    }

    @Override
    public void read(PortableReader reader)
            throws IOException {
        name = reader.readUTF("n");
        register = reader.readBoolean("r");
        ObjectDataInput in = reader.getRawDataInput();
        cacheEntryListenerConfiguration = in.readObject();
        target = new Address();
        target.readData(in);
    }

    @Override
    public Permission getRequiredPermission() {
        return new CachePermission(name, ActionConstants.ACTION_LISTEN);
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{cacheEntryListenerConfiguration};
    }

    @Override
    public String getMethodName() {
        return "registerCacheEntryListener";
    }

    @Override
    public String getDistributedObjectName() {
        return name;
    }

}
