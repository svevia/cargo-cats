package com.contrast.dataservice.repository;

import com.contrast.dataservice.entity.Shipment;
import com.contrast.dataservice.entity.User;
import com.contrast.dataservice.entity.Cat;
import com.contrast.dataservice.entity.Address;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "shipments", path = "shipments")
public interface ShipmentRepository extends PagingAndSortingRepository<Shipment, Long>, CrudRepository<Shipment, Long> {

  List<Shipment> findByTrackingId(@Param("trackingId") String trackingId);
  List<Shipment> findByStatus(@Param("status") String status);
  List<Shipment> findByObjOwner(@Param("objOwner") User objOwner);
  List<Shipment> findByCat(@Param("cat") Cat cat);
  List<Shipment> findByToAddress(@Param("toAddress") Address toAddress);
  List<Shipment> findByFromAddress(@Param("fromAddress") Address fromAddress);

}
