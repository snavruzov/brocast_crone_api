package com.dgtz.api.crone.services;

import com.dgtz.api.crone.beans.LiveLockBean;
import com.dgtz.mcache.api.factory.Constants;
import com.dgtz.mcache.api.factory.RMemoryAPI;
import com.dgtz.mcache.api.utils.GsonInsta;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
@Component
public class SchedulerTask {
    private static final Logger log = LoggerFactory.getLogger(SchedulerTask.class);

    @Autowired
    private RabbitTemplate template;

    @Scheduled(fixedRate = 10000)
    public void reportEventScan() {
        log.info("The time is now {}", new Date().toString());
        Set<String> llocks = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.LIVE_KEY + "system:livelock:set."+Constants.REGION);

        llocks.forEach(idl -> {
            try {
                String val = RMemoryAPI.getInstance().pullElemFromMemory(Constants.LIVE_KEY + "system:livelock:" + idl);
                LiveLockBean lockBean = GsonInsta.getInstance().fromJson(val, LiveLockBean.class);

                log.info("WAIT FOR LIVE COME BACK {}", idl);
                if (val!=null && (System.currentTimeMillis() - lockBean.time >= 31000)) {
                    log.info("Terminating delayed live {}", lockBean.idLive);
                    RMemoryAPI.getInstance().delFromSetElem(Constants.LIVE_KEY + "system:livelock:set."+Constants.REGION, idl);
                    RMemoryAPI.getInstance().delFromMemory(Constants.LIVE_KEY + "system:livelock:" + idl);
                    initLiveProccessing(lockBean.idUser, lockBean.idLive, lockBean.debate);
                    log.info("Sent to the queue {}", idl);
                    FileUtil.mountStorageDisk();
                    FileUtil.copyDir("/opt/live/hls_pro/"+lockBean.streamFull,
                                    "/opt/hls_fragments/hls_pro/"+lockBean.streamFull);
                    FileUtil
                            .copyFile("/opt/live/rec/"+lockBean.streamFull+".flv"
                                    ,"/opt/dump/live/rec/"+lockBean.streamFull+".flv");

                    template.convertAndSend(lockBean.queueName, lockBean.idUser + "∞"
                            + lockBean.idLive + "∞"
                            + lockBean.app + "∞"
                            + lockBean.streamFull + "∞"
                            + lockBean.rotation);
                } else if(val == null){
                    RMemoryAPI.getInstance().delFromSetElem(Constants.LIVE_KEY + "system:livelock:set."+Constants.REGION, idl);
                    RMemoryAPI.getInstance().delFromMemory(Constants.LIVE_KEY + "system:livelock:" + idl);
                }
            } catch (Exception e){
                log.error("Error while terminating delayed live",e);
            }
        });
    }


    private void initLiveProccessing(String idUser, String idLive, boolean debate) {
        RMemoryAPI.getInstance().pushElemToMemory(Constants.LIVE_KEY + "system:stop:" + idLive, -1, "1");
        RMemoryAPI.getInstance()
                .pushHashToMemory(Constants.MEDIA_KEY + idLive, "stop-time", (RMemoryAPI.getInstance().currentTimeMillis() - 30000) + "");

        // TODO store seconds not millisecods
        if (!debate) {
            sendWebsocketMessage(idUser, idLive, idLive, 3);
            RMemoryAPI.getInstance().delFromMemory(Constants.LIVE_KEY + "debate.status:" + idLive);
        } else {
            String idHost = RMemoryAPI.getInstance().pullHashFromMemory(Constants.MEDIA_KEY + idLive, "debate.author");
            sendWebsocketMessage(idUser, idLive, idHost, 15);
            RMemoryAPI.getInstance().delFromMemory(Constants.LIVE_KEY + "debate.status:" + idLive);
            RMemoryAPI.getInstance().delFromMemory(Constants.LIVE_KEY + "debate.queue:" + idHost);
        }

        RMemoryAPI.getInstance().pushElemToMemory(Constants.LIVE_KEY + "live:" + idLive, 3, "1"); //Lock Live event
    }

    private void sendWebsocketMessage(String idUser, String idLive, String hostID, int type){
        String hash = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idUser, "hash");
        JsonNode node =
                new JsonNode("{\"time\":\"12345678\",\"idHash\":\"" + hash + "\"," +
                        "\"text\":\" \",\"idMedia\":\"" + idLive + "\",\"wsType\":\""+type+"\"}");
        Future<HttpResponse<JsonNode>> rStatus =
                Unirest.post(Constants.WEBSOCKET_URL + "ws/media/" + hostID)
                        .header("Content-Type", "application/json")
                        .body(node)
                        .asJsonAsync(new Callback<JsonNode>() {
                            public void failed(UnirestException e) {
                                log.info("The WS request has failed");
                            }

                            public void completed(HttpResponse<JsonNode> response) {
                                int code = response.getStatus();
                                log.info("Completed {}", code);
                            }

                            public void cancelled() {
                                log.debug("The request has been cancelled");
                            }
                        });
    }



    /*private void updateEventsStatus(){
        try {
            Set<String> evlist =  RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.MEDIA_KEY + "events");
            evlist.forEach(idm -> {
               String time = RMemoryAPI.getInstance().pullHashFromMemory(Constants.MEDIA_KEY + idm, "evnt_time");
                if(time!=null) {
                    Long evntStart = Long.valueOf(time);
                    Long diff = evntStart - System.currentTimeMillis();
                    if (diff <= 3600000 && diff>=0) {
                        updateMediaRiakData(idm, false);
                    } else if (diff < 0) {
                        updateMediaRiakData(idm, true);
                    }
                }
            });
        }catch (Exception e){
            log.error("error ", e);
        }
    }

    private void updateMediaRiakData(String idm, boolean disable){
        RiakTP transport = RiakAPI.getInstance();
        IRiakQueryFactory queryFactory = new RiakQueryFactory(transport);
        DcMediaEntity md = queryFactory.queryMediaDataByID(Long.valueOf(idm));

        if(disable){
            UsersShelf usersShelf = new UsersShelf();
            usersShelf.removeTheMediaByOwner(md.idUser, md.idMedia);
            RMemoryAPI.getInstance().delFromSetElem(Constants.MEDIA_KEY + "events", idm);
        } else {
            IRiakSaveFactory saveFactory = new RiakSaveFactory(transport);
            saveFactory.updMediaContent(md);
        }
    }*/
}
