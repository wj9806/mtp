package com.mtp.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 应用程序信息实体类，包含应用名称和实例列表
 */
@Data
public class ApplicationInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applicationName;
    private Set<InstanceInfo> instances;

    public ApplicationInfo() {
        this.instances = new HashSet<>();
    }

    public ApplicationInfo(String applicationName) {
        this.applicationName = applicationName;
        this.instances = new HashSet<>();
    }

    public void addInstance(InstanceInfo instance) {
        this.instances.add(instance);
    }

    public int getInstanceCount() {
        return instances.size();
    }

    /**
     * 应用实例信息，包含IP地址和端口
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstanceInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String ip;
        private Integer port;
        private String status;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstanceInfo that = (InstanceInfo) o;
            return Objects.equals(ip, that.ip) && Objects.equals(port, that.port) && Objects.equals(status, that.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port, status);
        }
    }
}