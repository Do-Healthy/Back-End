package gangdong.diet.domain.post.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public class SliceUtil {
    private static final String ORDER_BY_UPDATED_DATE = "updatedDate";

    /**
     * 페이징 정보로부터 limit 값 가져오기
     */
    public static int getLimit(Pageable pageable) {
        return pageable.getPageSize() + 1;
    }

    /**
     * 페이징 정보로부터 정렬 조건 생성
     */
//    public static OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
//        return pageable.getSort().stream()
//                .map(order -> {
//                    // 서비스에서 넣어준 DESC or ASC 를 가져오기
//                    Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
//                    // 서비스에서 넣어준 정렬 조건을 스위치 케이스 문을 활용하여 세팅
//                    switch (order.getProperty()) {
//                        case ORDER_BY_UPDATED_DATE:
//                            return new OrderSpecifier<>(direction, diary.updatedDate);
//                        default:
//                            return new OrderSpecifier<>(direction, diary.createdDate);
//                    }
//                })
//                .findFirst()
//                .orElseGet(() -> new OrderSpecifier<>(Order.DESC, diary.createdDate));
//    }

    /**
     * 리스트를 Slice로 변환
     */
    public static <T, R> SliceImpl<R> toSlice(Pageable pageable, List<T> lst, Function<T, R> mapper) {
        boolean hasNext = lst.size() > pageable.getPageSize();
        if (hasNext) {
            lst.remove(pageable.getPageSize());
        }
        return new SliceImpl<>(
                lst.stream().map(mapper).collect(Collectors.toList()),
                pageable,
                hasNext
        );
    }
}