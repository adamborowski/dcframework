package pl.adamborowski.dcframework.remote.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class ShutdownMessage<Result> implements Serializable {
    private final Result result;
}
