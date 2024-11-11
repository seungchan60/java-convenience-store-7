package store.view;

import camp.nextstep.edu.missionutils.Console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputView {

    private static final Pattern INPUT_PATTERN = Pattern.compile("\\[([가-힣]+)-(\\d+)](,\\[([가-힣]+)-(\\d+)])*");

    public String readPurchaseInput() {
        System.out.println("구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");

        String input = Console.readLine();
        while (!isValidInputFormat(input)) {
            System.out.println("[ERROR] 올바르지 않은 형식으로 입력했습니다. 다시 입력해 주세요.");
            input = Console.readLine();
        }
        return input;
    }

    private boolean isValidInputFormat(String input) {
        Matcher matcher = INPUT_PATTERN.matcher(input);
        return matcher.matches();
    }
}
