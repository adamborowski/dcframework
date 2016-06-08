package pl.adamborowski.dcframework.comm.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class ShutdownMessage<Result> implements Serializable {
    public final Result result;
}
