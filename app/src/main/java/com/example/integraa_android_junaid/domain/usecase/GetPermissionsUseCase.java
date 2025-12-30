package com.example.integraa_android_junaid.domain.usecase;

import com.example.integraa_android_junaid.data.repository.PermissionRepository;

public class GetPermissionsUseCase {
    private final PermissionRepository permissionRepository;

    public GetPermissionsUseCase(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void execute(PermissionRepository.PermissionsCallback callback) {
        permissionRepository.getPermissions(callback);
    }

    public void forceRefresh(PermissionRepository.PermissionsCallback callback) {
        permissionRepository.forceRefresh(callback);
    }
}

