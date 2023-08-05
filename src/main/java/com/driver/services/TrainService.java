package com.driver.services;
import java.time.format.DateTimeFormatter;
import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train curr=new Train();
        curr.setDepartureTime(trainEntryDto.getDepartureTime());
        curr.setNoOfSeats(trainEntryDto.getNoOfSeats());
        String route="";
        List<Station> stns=trainEntryDto.getStationRoute();
        for(int i=0;i<stns.size();i++){
            if(i!=stns.size()-1) {
                route += stns.get(i).name() + ",";
            }
            else{
                route+=stns.get(i).name();
            }
        }
        Train savedTrain=trainRepository.save(curr);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
          Optional<Train> sts=trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
          int seats=sts.get().getNoOfSeats();
          List<Ticket> tkts=sts.get().getBookedTickets();
          Station fromLocation=seatAvailabilityEntryDto.getFromStation();
          String Rt[]=sts.get().getRoute().split(",");
          int extra=0;
          for(Ticket T:tkts){
           List<String> rts=getRoute(T,Rt);
           boolean containsFrom=false;
           for(int i=0;i<rts.size();i++){
               if(rts.get(i).equals(seatAvailabilityEntryDto.getFromStation())) {
                   containsFrom = true;
                   break;
               }
           }
           if(containsFrom==true){
               if(T.getToStation().equals(seatAvailabilityEntryDto.getFromStation())){
                   extra++;
               }
               else{

               }
           }
           else{
               extra++;
           }
          }
          int avSeats=seats-tkts.size();

       return extra+avSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
      Optional<Train> tr=trainRepository.findById(trainId);
      String arr[]=tr.get().getRoute().split(",");
      boolean isPresent=false;
      for(int i=0;i<arr.length;i++){
          if(arr[i].equals(station)){
              isPresent=true;
              break;
          }
      }
      if(isPresent==false){
          throw new Exception("Train is not passing from this station");
      }
      List<Ticket> tkts=tr.get().getBookedTickets();
      int peoples=0;
      for(Ticket tk:tkts){
          if(tk.getFromStation().equals(station)){
              peoples++;
          }
      }

        return peoples;
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

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        int maxAge=0;
        Optional<Train> tr=trainRepository.findById(trainId);
        Train train=tr.get();
        List<Ticket> tkts=train.getBookedTickets();
        if(tkts.size()>0) {
            for (int i = 0; i < tkts.size(); i++) {
                List<Passenger> passengers = tkts.get(0).getPassengersList();
                for (Passenger p : passengers) {
                    if (p.getAge() > maxAge) {
                        maxAge = p.getAge();
                    }
                }
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
         List<Train> trains=trainRepository.findAll();
         List<Integer> ts=new ArrayList<>();
         for(Train T:trains){
             String arr[]=T.getRoute().split(",");
             int idx=-1;
             for(int i=0;i<arr.length;i++){
                 if(arr[i].equals(station)){
                     idx=i;
                 }
             }
             if(idx!=1){
                 LocalTime time=T.getDepartureTime().plusHours(idx);
                 if(time.compareTo(startTime)>=0 && time.compareTo(endTime)<=0){
                     ts.add(T.getTrainId());
                 }
             }
         }
        return ts;
    }

}
