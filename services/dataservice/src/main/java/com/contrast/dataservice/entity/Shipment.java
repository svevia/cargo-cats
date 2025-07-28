package com.contrast.dataservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import java.util.UUID;

@Entity
public class Shipment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "obj_owner")
  private User objOwner;

  @ManyToOne
  @JoinColumn(name = "to_address")
  private Address toAddress;

  @ManyToOne
  @JoinColumn(name = "from_address")
  private Address fromAddress;

  @Column(unique = true, nullable = false)
  private String trackingId;

  @ManyToOne
  @JoinColumn(name = "cat_id")
  private Cat cat;

  @Column(nullable = false)
  private String status = "open";

  @Column
  private String creditCard;

  @Column
  private String notificationUrl;

  @Column(nullable = false)
  private boolean notified = true;

  @PrePersist
  protected void onCreate() {
    if (trackingId == null) {
      trackingId = "TRACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public User getObjOwner() {
    return objOwner;
  }

  public void setObjOwner(User objOwner) {
    this.objOwner = objOwner;
  }

  public Address getToAddress() {
    return toAddress;
  }

  public void setToAddress(Address toAddress) {
    this.toAddress = toAddress;
  }

  public Address getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(Address fromAddress) {
    this.fromAddress = fromAddress;
  }

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
  }

  public Cat getCat() {
    return cat;
  }

  public void setCat(Cat cat) {
    this.cat = cat;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    // If status is changing, reset notified to false so webhook notifications can be sent
    if (this.status != null && !this.status.equals(status)) {
      this.notified = false;
    }
    this.status = status;
  }

  public String getCreditCard() {
    return creditCard;
  }

  public void setCreditCard(String creditCard) {
    this.creditCard = creditCard;
  }

  public String getNotificationUrl() {
    return notificationUrl;
  }

  public void setNotificationUrl(String notificationUrl) {
    this.notificationUrl = notificationUrl;
  }

  public boolean isNotified() {
    return notified;
  }

  public void setNotified(boolean notified) {
    this.notified = notified;
  }
}
