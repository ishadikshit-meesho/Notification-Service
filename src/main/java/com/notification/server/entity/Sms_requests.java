package com.notification.server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name="sms_requests")
public class Sms_requests{
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="id")
    private int id;

    @Setter
    @Getter
    @Column(name="phone_number")
    private String phone_number;

    //Getters and Setters
    @Setter
    @Getter
    @Column(name="message")
    private String message;

    @Setter
    @Getter
    @Column(name="status")
    private String status;

    @Setter
    @Getter
    @Column(name="created_at")
    private Date created_at;

    @Setter
    @Getter
    @Column(name="updated_at")
    private Date updated_at;

    @Setter
    @Getter
    @Column(name="failure_code")
    private int failure_code;

    @Setter
    @Getter
    @Column(name="failure_comments")
    private String failure_comments;

    public Sms_requests() {}

    //Constructor
    public Sms_requests(String status, String phone_number, String message, Date created_at) {
        this.status = status;
        this.phone_number = phone_number;
        this.message = message;
        this.created_at = created_at;
        this.updated_at = created_at;
        this.failure_code = 0;
        this.failure_comments = "NIL";
    }

    @Override
    public String toString() {
        return "Sms_requests{" +
                "id=" + id +
                ", phone_number='" + phone_number + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", failure_code=" + failure_code +
                ", failure_comments='" + failure_comments + '\'' +
                '}';
    }
}
