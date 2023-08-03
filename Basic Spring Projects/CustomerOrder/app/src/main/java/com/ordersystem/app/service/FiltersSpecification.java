package com.ordersystem.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.*;
import com.ordersystem.app.dto.Searchreq;
import com.ordersystem.app.dto.Req;

@Service
public class FiltersSpecification<T> {

     public Specification<T> getSearchSpecification(List<Searchreq> searchreqs, Req.Operators globalOperator) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            for(Searchreq requestDto : searchreqs){

                switch (requestDto.getOperation()){

                    case EQUAL:
                        Predicate equal;
                        if (root.get(requestDto.getColumn()).getJavaType() == Boolean.class){
                            equal = criteriaBuilder.and(criteriaBuilder.conjunction(), criteriaBuilder.equal(root.get(requestDto.getColumn()), Boolean.parseBoolean(requestDto.getValue())));
                        }else{
                            equal = criteriaBuilder.equal(root.get(requestDto.getColumn()), requestDto.getValue());
                        }
                        predicates.add(equal);
                        break;

                    case LIKE:
                        Predicate like = criteriaBuilder.like(root.get(requestDto.getColumn()), "%"+requestDto.getValue()+"%");
                        predicates.add(like);
                        break;
                    case STARTS:
                        Predicate starts = criteriaBuilder.like(root.get(requestDto.getColumn()), requestDto.getValue()+"%");
                        predicates.add(starts);
                        break;
                    case ENDS:
                    Predicate ends = criteriaBuilder.like(root.get(requestDto.getColumn()), "%"+requestDto.getValue());
                    predicates.add(ends);
                    break;

                    case IN:
                        String[] split = requestDto.getValue().split(",");
                        Predicate in = root.get(requestDto.getColumn()).in(Arrays.asList(split));
                        predicates.add(in);
                        break;

                    case GREATER_THAN:
                        Predicate greaterThan = criteriaBuilder.greaterThan(root.get(requestDto.getColumn()), requestDto.getValue());
                        predicates.add(greaterThan);
                        break;

                    case LESS_THAN:
                        Predicate lessThan = criteriaBuilder.lessThan(root.get(requestDto.getColumn()), requestDto.getValue());
                        predicates.add(lessThan);
                        break;

                    case BETWEEN:
                        String[] split1 = requestDto.getValue().split(",");
                        Predicate between = criteriaBuilder.between(root.get(requestDto.getColumn()), Long.parseLong(split1[0]),Long.parseLong( split1[1]));
                        predicates.add(between);
                        break;

                    case JOIN:
                        Predicate join = criteriaBuilder.equal(root.join(requestDto.getJoinTable()).get(requestDto.getColumn()), requestDto.getValue());
                        predicates.add(join);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + requestDto.getOperation());
                }

            }

            if(globalOperator.equals(Req.Operators.AND)) {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }else if(globalOperator.equals(Req.Operators.OR)){
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            }else{
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}