package com.crediya.r2dbc.state;

import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.r2dbc.entity.StateEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Repository
public class StateReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        State,
        StateEntity,
        String,
        StateReactiveRepository
        > implements StateRepository {
    public StateReactiveRepositoryAdapter(StateReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, State.class));
    }

    @Override
    public Mono<State> getStateByName(String name) {
        return this.repository.findByName(name)
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, State.class))
                .onErrorMap(e -> new RuntimeException("Error al consultar estado por nombre", e));
    }

    @Override
    public Mono<State> getStateById(String id) {
        return this.repository.findByStateId(id)
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, State.class))
                .onErrorMap(e -> new RuntimeException("Error al consultar estado por ID", e));
    }
}
