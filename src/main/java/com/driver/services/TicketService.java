package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Optional<Passenger> ps=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        Optional<Train> trn=trainRepository.findById(bookTicketEntryDto.getTrainId());
        Train train=trn.get();
        String arr[]=train.getRoute().split(",");
        boolean isFrom=false;
        boolean isTo=false;
        List<String> route=new ArrayList<>();
        for(int i=0;i<arr.length;i++){

            if(arr[i].equals(bookTicketEntryDto.getFromStation())){
                isFrom=true;
            }
            if(arr[i].equals(bookTicketEntryDto.getToStation())){
                isTo=true;
            }
        }
        if(isFrom==false || isTo==false){
            throw new Exception("Invalid stations");
        }
       int i=0;
        while(i<arr.length){
            if(arr[i].equals(bookTicketEntryDto.getFromStation())){
                route.add(arr[i]);
                i++;
                while(arr[i].equals(bookTicketEntryDto.getToStation())){
                    route.add(arr[i]);
                    i++;
                }
            }
        }
        int seats=train.getNoOfSeats();
        List<Ticket> tkts=train.getBookedTickets();

        String Rt[]=arr;
        int extra=0;
        for(Ticket T:tkts){
            List<String> rts=getRoute(T,Rt);
            boolean containsFrom=false;
            for(int k=0;k<rts.size();k++){
                if(rts.get(k).equals(bookTicketEntryDto.getFromStation())) {
                    containsFrom = true;
                    break;
                }
            }
            if(containsFrom==true){
                if(T.getToStation().equals(bookTicketEntryDto.getFromStation())){
                    extra++;
                }
                else{

                }
            }
            else{
                extra++;
            }
        }
        int avSeats=seats-tkts.size()+extra;
        if(avSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
            Ticket ticket=new Ticket();
            ticket.setFromStation(bookTicketEntryDto.getFromStation());
            ticket.setToStation(bookTicketEntryDto.getToStation());
            List<Integer> passengerIds=bookTicketEntryDto.getPassengerIds();
            int fare=(route.size()-1)*300*passengerIds.size();
            ticket.setTotalFare(fare);
            Ticket savedTicket=ticketRepository.save(ticket);
            for(Integer j:passengerIds){
                Optional<Passenger> p=passengerRepository.findById(j);
                savedTicket.getPassengersList().add(p.get());
                p.get().getBookedTickets().add(savedTicket);
            }
            savedTicket.setTrain(train);
            train.getBookedTickets().add(savedTicket);

           Train savedTrain=trainRepository.save(train);
           return savedTrain.getBookedTickets().get(savedTrain.getBookedTickets().size()-1).getTicketId();
    }
    public List<String> getRoute(Ticket tkt,String Route[]){
        List<String> rts=new ArrayList<>();
        int i=0;
        while(i<Route.length){
            if(Route[i].equals(tkt.getFromStation())){
                rts.add(Route[i]);
                i++;
                while(Route[i].equals(tkt.getToStation())){
                    rts.add(Route[i]);
                    i++;
                }
                break;
            }
        }
        return rts;
    }
}
