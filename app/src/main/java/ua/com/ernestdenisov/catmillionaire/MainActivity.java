package ua.com.ernestdenisov.catmillionaire;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    /**
     * Создаем переменные для наших виджетов.
     * Создаем целочисленные переменные, для текущего счёта, количества улова при каждом клике и в секунду,
     * а также общий счет, влияющий на надписи вверху экрана.
     * priceOne-Ten содержат информацию о стоимости покупок для каждой кнопки. Эти значения не константны
     * и при каждой покупке растут через сложный процент на 1/10 от текущей цены.
     * булевы переменные hascat, hasbank призваны блокировать следующие покупки без приобретения предыдущих,
     * а именно: котят нельзя купить без покупки кошки, а дом не доступен без банковского счёта.
     * Медиаплеер нужен, чтобы запустить песню в случае победы (когда total = 1млн)
     */
    TextView textViewAmount, textViewTotal, textViewPerclick, textViewPersec, textViewSuccess;
    Button buttonClick, button1, button2, button3, button4, button5, button6, button7, button8, button9, button10;
    LinearLayout linearLayoutMain;
    int amount = 0, click = 1, persec = 0, total = 0;
    int priceOne = 50, priceTwo = 300, priceThree = 1000, priceFour = 4000, priceFive = 12000,
            priceSix = 25000, priceSeven = 50000, PriceEight = 100000, proceNine = 250000, priceTen = 500000;
    boolean hascat = false, hasbank = false;
    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewAmount = (TextView) findViewById(R.id.textViewAmount);
        textViewTotal = (TextView) findViewById(R.id.textViewTotal);
        textViewPerclick = (TextView) findViewById(R.id.textViewPerclick);
        textViewPersec = (TextView) findViewById(R.id.textViewPersec);
        textViewSuccess = (TextView) findViewById(R.id.textViewSuccess);

        textViewPerclick.setText("мышей за\n клик " + click);
        textViewPersec.setText("мышей в\n секунду " + persec);
        textViewTotal.setText("Всего поймано мышей " + total);
        textViewSuccess.setText("Сейчас ты маленький бездомный котенок. Лови мышей и ты станешь настоящим котом.");

        //назначаем кнопкам текст с указанием названия и получаемой выгоды
        button1 = (Button) findViewById(R.id.button1);
        button1.setText("МЫШЕЛОВКА\n" + "цена " + priceOne + "\n" + "мышей в секунду 1\n");
        button2 = (Button) findViewById(R.id.button2);
        button2.setText("КОТ-ПОМОЩНИК\n" + "цена " + priceTwo + "\n" + "мышей за клик 1\n" + "мышей в секунду 2");
        button3 = (Button) findViewById(R.id.button3);
        button3.setText("ПРИМАНКА\n" + "цена " + priceThree + "\n" + "мышей за клик 2\n" + "мышей в секунду 3");
        button4 = (Button) findViewById(R.id.button4);
        button4.setText("КОШАЧИЙ КОРМ\n" + "цена " + priceFour + "\n" + "мышей за клик 3\n" + "мышей в секунду 5");
        button5 = (Button) findViewById(R.id.button5);
        button5.setText("КОЛБАСА\n" + "цена " + priceFive + "\n" + "мышей за клик 5\n" + "мышей в секунду 7");
        button6 = (Button) findViewById(R.id.button6);
        button6.setText("ДОМИК\n" + "цена " + priceSix + "\n" + "мышей за клик 10\n" + "мышей в секунду 10");
        button7 = (Button) findViewById(R.id.button7);
        button7.setText("КОШЕЧКА\n" + "цена " + priceSeven + "\n" + "мышей за клик 12\n" + "мышей в секунду 20");
        button8 = (Button) findViewById(R.id.button8);
        button8.setText("КОТЯТА-ПОМОЩНИКИ\n" + "цена " + PriceEight + "\n" + "мышей за клик 15\n" + "мышей в секунду 25");
        button9 = (Button) findViewById(R.id.button9);
        button9.setText("ДЕПОЗИТ В БАНКЕ\n" + "цена " + proceNine + "\n" + "мышей за клик 20\n" + "мышей в секунду 35");
        button10 = (Button) findViewById(R.id.button10);
        button10.setText("ОСОБНЯК НА РУБЛЕВКЕ\n" + "цена " + priceTen + "\n" + "мышей за клик 50\n" + "мышей в секунду 50");

        //для удобства кнопку клика выносим в отдельный обработчик
        buttonClick = (Button) findViewById(R.id.buttonClick);
        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = amount + click;
                total = total + click;
                textViewAmount.setText("Мышей на счету " + amount);
                textViewTotal.setText("Всего поймано мышей " + total);
                onResume();
            }
        });

        textViewAmount.setText("Мышей на счету " + amount);
        textViewTotal.setText("Всего поймано мышей " + total);
    }

    /**
     * метод onClick обрабатывает нажатия каждой из 10-ти кнопок.
     * Если на счету меньше, чем стоимость покупки, то выводится предупреждение.
     * Если средств достаточно, то от счёта отнимается стоимость покупки, а стоимость вырастает на 10%.
     * Увеличиваются значения за клик и за секунду с обновлением этой информации на экране
     * Запускается дочерний поток, в котором происходит ежесекундный рост счета и общей суммы за игру,
     * далее значения возвращаются в основной поток с обновлением информации на экране.
     * В кнопках button8, button10 идет проверка предварительных покупок.
     * Текст при отображении имеет обводку для лучшего визуального восприятния.
     */
    public void onClick(View view) {
        textViewAmount.setText("Мышей на счету " + amount);
        textViewPerclick.setText("мышей за\n клик " + click);
        textViewPersec.setText("мышей в\n секунду " + persec);
        onResume();
        switch (view.getId()) {
            case R.id.button1:
                if (amount < priceOne) {
                    Toast.makeText(getApplicationContext(), "А денег-то не хватает", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceOne;
                    priceOne = priceOne + (priceOne / 10);
                    persec++;
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button1.setText("МЫШЕЛОВКА\n" + "цена " + priceOne + "\n" + "мышей в секунду 1\n");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount++;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total++;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button2:
                if (amount < priceTwo) {
                    Toast.makeText(getApplicationContext(), "Купишь, когда заработаешь", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceTwo;
                    click = click + 1;
                    priceTwo = priceTwo + (priceTwo / 10);
                    persec += 2;
                    total += 1;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button2.setText("КОТ-ПОМОЩНИК\n" + "цена " + priceTwo + "\n" + "мышей за клик 1\n" + "мышей в секунду 2");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 2;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 2;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button3:
                if (amount < priceThree) {
                    Toast.makeText(getApplicationContext(), "Кризис, что поделать", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceThree;
                    click = click + 2;
                    priceThree = priceThree + (priceThree / 10);
                    persec += 3;
                    total += 2;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button3.setText("ПРИМАНКА\n" + "цена " + priceThree + "\n" + "мышей за клик 2\n" + "мышей в секунду 3");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 3;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 3;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button4:
                if (amount < priceFour) {
                    Toast.makeText(getApplicationContext(), "Это для тебя дорого", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceFour;
                    click = click + 3;
                    priceFour = priceFour + (priceFour / 10);
                    persec += 5;
                    total += 3;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button4.setText("КОШАЧИЙ КОРМ\n" + "цена " + priceFour + "\n" + "мышей за клик 3\n" + "мышей в секунду 5");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 5;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 5;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button5:
                if (amount < priceFive) {
                    Toast.makeText(getApplicationContext(), "Приходи, когда будут деньги, котейко", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceFive;
                    click = click + 5;
                    priceFive = priceFive + (priceFive / 10);
                    persec += 7;
                    total += 5;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button5.setText("КОЛБАСА\n" + "цена " + priceFive + "\n" + "мышей за клик 5\n" + "мышей в секунду 7");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 7;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 7;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button6:
                if (amount < priceSix) {
                    Toast.makeText(getApplicationContext(), "Надоело жить на улице в коробке?", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceSix;
                    click = click + 10;
                    priceSix = priceSix + (priceSix / 10);
                    persec += 10;
                    total += 5;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button6.setText("ДОМИК\n" + "цена " + priceSix + "\n" + "мышей за клик 10\n" + "мышей в секунду 10");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 10;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 10;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button7:
                if (amount < priceSeven) {
                    Toast.makeText(getApplicationContext(), "Не твоего поля ягода, пока что", Toast.LENGTH_SHORT).show();
                } else {
                    hascat = true;
                    amount = amount - priceSeven;
                    click = click + 20;
                    priceSeven = priceSeven + (priceSeven / 10);
                    persec += 12;
                    total += 20;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button7.setText("КОШЕЧКА\n" + "цена " + priceSeven + "\n" + "мышей за клик 12\n" + "мышей в секунду 20");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 12;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 12;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button8:
                if(hascat){button8.setBackgroundResource(R.drawable.kitty);
                if (amount < PriceEight) {
                    Toast.makeText(getApplicationContext(), "Котята не из дешевых", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - PriceEight;
                    click = click + 25;
                    PriceEight = PriceEight + (PriceEight / 10);
                    persec += 15;
                    total += 25;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button8.setText("КОТЯТА\n" + "цена " + PriceEight + "\n" + "мышей за клик 15\n" + "мышей в секунду 25");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 15;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 15;
                                }
                            });
                        }
                    }, 0, 1000);
                }}else {Toast.makeText(getApplicationContext(), "Сначала заведи кошечку", Toast.LENGTH_SHORT).show();}
                break;
            case R.id.button9:
                if (amount < proceNine) {
                    Toast.makeText(getApplicationContext(), "Тебя с такими деньгами даже на порог не пустят", Toast.LENGTH_SHORT).show();
                } else {
                    hasbank = true;
                    amount = amount - proceNine;
                    click = click + 35;
                    proceNine = proceNine + (proceNine / 10);
                    persec += 20;
                    total += 35;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button9.setText("ДЕПОЗИТ В БАНКЕ\n" + "цена " + proceNine + "\n" + "мышей за клик 20\n" + "мышей в секунду 35");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 20;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 20;
                                }
                            });
                        }
                    }, 0, 1000);
                }
                break;
            case R.id.button10:
                if(hasbank){button10.setBackgroundResource(R.drawable.bighouse);
                if (amount < priceTen) {
                    Toast.makeText(getApplicationContext(), "Захотел красивой жизни? скоро все будет", Toast.LENGTH_SHORT).show();
                } else {
                    amount = amount - priceTen;
                    click = click + 50;
                    priceTen = priceTen + (priceTen / 10);
                    persec += 50;
                    total += 50;
                    textViewTotal.setText("Всего поймано мышей " + total);
                    textViewPerclick.setText("мышей за\n клик " + click);
                    textViewPersec.setText("мышей в\n секунду " + persec);
                    button10.setText("ОСОБНЯК\n" + "цена " + priceTen + "\n" + "мышей за клик 50\n" + "мышей в секунду 50");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewAmount.setText("Мышей на счету " + Integer.toString(amount));
                                    amount += 50;
                                    textViewTotal.setText("Всего поймано мышей " + Integer.toString(total));
                                    total += 50;
                                }
                            });
                        }
                    }, 0, 1000);
                }}else {Toast.makeText(getApplicationContext(), "Сперва открой депозит в банке", Toast.LENGTH_SHORT).show();}
                break;
        }
    }

    public void startAudio(){
        mediaPlayer = MediaPlayer.create(this, R.raw.backsong);
        mediaPlayer.start();
        mediaPlayer.setLooping(false);
    }

    /**
     * Успехи в игре определяют надпись в шапке экрана.
     * Заработав миллион фон меняется, включается музыка, играющая несколько минут без повтора,
     * меняется фон и цвет шрифта, чтобы было лучше видно
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (total > 500 & total < 1000) {
            textViewSuccess.setText("Молодец, так держать");
        }
        if (total >= 1000 & total < 10000) {
            textViewSuccess.setText("Прохожие удивляются твоим успехам");
        }
        if (total >= 10000 & total < 100000) {
            textViewSuccess.setText("Ты стал котом");
        }
        if (total >= 100000 & total < 250000) {
            textViewSuccess.setText("Коты с других дворов приходят на тебя посмотреть");
        }
        if (total >= 250000 & total < 500000) {
            textViewSuccess.setText("О тебе пишут в газетах");
        }
        if (total >= 500000 & total < 1000000) {
            textViewSuccess.setText("Президент вручил тебе награду за заслуги перед отечеством");
        }
        if (total >= 1000000) {
            textViewSuccess.setText("Более успешных котов мир не знает");
            linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
            linearLayoutMain.setBackgroundResource(R.drawable.moneyback);
            textViewAmount.setTextColor(Color.YELLOW);
            textViewTotal.setTextColor(Color.YELLOW);
            textViewPerclick.setTextColor(Color.YELLOW);
            textViewPersec.setTextColor(Color.YELLOW);
            textViewSuccess.setTextColor(Color.YELLOW);
            startAudio();
        }
    }
}
