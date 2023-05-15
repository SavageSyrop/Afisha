package ru.it.lab.service;


import net.devh.boot.grpc.server.service.GrpcService;
import ru.it.lab.AdminServiceGrpc;


@GrpcService
public class AdminServerService extends AdminServiceGrpc.AdminServiceImplBase {



//    @Override
//    public void requestSupport(ru.it.lab.SupportRequest request, StreamObserver<Info> responseObserver) {
//        SupportRequest supportRequest = new SupportRequest();
//        User user = userDao.getById(request.getUserId());
//        supportRequest.setUser(user);
//
//    }


}
