package com.furniture.domain;

public enum AccountStatus {

    PENDING_VERIFICATION, //Tai khoan duoc tao nhung chua xac thuc
    ACTIVE, //Tai khoan da duoc kich hoat va dang hoat dong
    SUSPENDED, // Tai khoan bi dinh chi hoat dong tam thoi
    DEACTIVATED, //Tai khoan bi vo hieu hoa
    BANNED, //Tai khoan bi cam vinh vien
    CLOSED, //Tai khoan bi dong do nguoi dung yeu cau
}
