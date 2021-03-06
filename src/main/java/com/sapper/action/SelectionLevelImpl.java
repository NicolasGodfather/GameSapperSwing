package com.sapper.action;

/**
 * TODO: Реализация проверки выбраного уровня пользователем, что повлияет на всю логику игры
 *
 * @author Asinovich_Nikolay
 * @since 03.05.2016.
 */

import com.sapper.gui.GuiCell;
import com.sapper.interfaces.*;
import com.sapper.levels.Easy;
import com.sapper.levels.Expert;
import com.sapper.levels.Medium;

import java.util.Random;

/**
 * Для изменения логики игры в каком-то из уровней, достаточно:
 *
 * 1. Расширить класс уровня добавив интерфейс LogicGame. После изменить логику игры в доступных методах
 *
 * 2. После переходим в этот класс и проверяем на null уровень в методе,
 *    где была изменена логика (как это релизовано в этом классе в методах sumBombs(), sumRow(), sumColumn() ),
 *    если данный уровень был выбран, то вызываем метод находящийся в этом уровне
 *    (в каком уже изменена логика), иначе запускаем стандартную логику находящуюся в этом классе
 */
public class SelectionLevelImpl implements SelectLevel,
                                             LogicGame,
                                               NumOfField {

    private Easy easy;
    private Medium medium;
    private Expert expert;

    private Cell[][] cells;

    @Override
    public Easy easy() {
        medium = null;
        expert = null;
        return easy = new Easy();
    }

    @Override
    public Medium medium() {
        easy = null;
        expert = null;
        return medium = new Medium();
    }

    @Override
    public Expert expert() {
        easy = null;
        medium = null;
        return expert = new Expert();
    }

    @Override
    public void loadBoardGame(Cell[][] cells) {
        this.cells = cells;
    }

    @Override
    public int sumBombs() {
        int sum = 0;
        if (easy == null && medium == null && expert != null)
            sum = expert.sumBombs();
        else if (easy == null && expert == null && medium != null)
            sum = medium.sumBombs();
        else if (easy != null)
            sum = easy.sumBombs();
        return sum;
    }

    @Override
    public int sumRow() {
        int row = 0;
        if (easy == null && medium == null && expert != null)
            row = expert.sumRow();
        else if (easy == null && expert == null && medium != null)
            row = medium.sumRow();
        else if (easy != null)
            row = easy.sumRow();
        return row;
    }

    @Override
    public int sumColumn() {
        int column = 0;
        if (easy == null && medium == null && expert != null)
            column = expert.sumColumn();
        else if (easy == null && expert == null && medium != null)
            column = medium.sumColumn();
        else if (easy != null)
            column = easy.sumColumn();
        return column;
    }

    @Override
    public Cell[][] sizeField() {
        return new Cell[sumRow()][sumColumn()];
    }

    @Override
    public boolean shouldBang(int x, int y) {
        final Cell selected = this.cells[x][y];
        // Если это бомба, и пользователь не предположил что это бомба, то мы взрываемся
        return selected.isBomb() && !selected.isSuggestBomb();
    }

    // Если пользователь всё разгадал, возвращаем истину
    @Override
    public boolean finish() {
        boolean finish = false;
        int check = 0;
        for (Cell[] row : this.cells)
            for (Cell cell : row)
                if ((cell.isSuggestBomb() && cell.isBomb()) ||
                        (cell.isSuggestEmpty() && !cell.isBomb()) || (!cell.isSuggestBomb() && cell.isBomb())
                        || cell.isSuggest1() || cell.isSuggest2() || cell.isSuggest3() || cell.isSuggest4()
                        || cell.isSuggest5() || cell.isSuggest6() || cell.isSuggest7() || cell.isSuggest8()) {
                    check++;
                }
        if (check == (sumRow() * sumColumn()))
            finish = true;
        return finish;
    }

    // Предположения пользователя (Бомба или пустая клетка)
    @Override
    public void suggest(int x, int y, boolean bomb) {
        if (!bomb)
            this.cells[x][y].suggestEmpty();
        if (bomb && !this.cells[x][y].isSuggestEmpty())
            this.cells[x][y].suggestBomb();
        else if (bomb && this.cells[x][y].isSuggestEmpty())
            System.out.println("Вы уже открыли эту клетку!\n");
    }

    // Проверка первого хода. Если на поле нет бомб, возвращаем истину
    @Override
    public boolean checkTheFirstMove() {
        boolean check = true;
        root: for (int i = 0; i < sumRow(); i++)
            for (int j = 0; j < sumColumn(); j++)
                if (this.cells[i][j].isBomb()) {
                    check = false;
                    break root;
                }
        return check;
    }

    // Очистка вокруг ячейки при первом ходе
    // Для того, чтобы у пользователя не открылась в начале игры только одна ячейка
    @Override
    public void clearAroundCell(int x, int y) {
        if (cells.length > 3) {
            if (y > 0) suggest(x, y - 1, false);
            if (y + 1 < sumColumn()) suggest(x, y + 1, false);
            if (x > 0) suggest(x - 1, y, false);
            if (x + 1 < sumRow()) suggest(x + 1, y, false);
            if (x > 0 && y > 0) suggest(x - 1, y - 1, false);
            if (x + 1 < sumRow() && y + 1 < sumColumn()) suggest(x + 1, y + 1, false);
            if (x > 0 && y + 1 < sumColumn()) suggest(x - 1, y + 1, false);
            if (x + 1 < sumRow() && y > 0) suggest(x + 1, y - 1, false);
        }
    }

    // Генерация бомб на поле
    @Override
    public void bombsGeneration() {
        Random random = new Random();
        int sumBombs = sumBombs();
        while (sumBombs > 0) {
            int row = random.nextInt(sumRow());
            int column = random.nextInt(sumColumn());
            if (!this.cells[row][column].isBomb() && !this.cells[row][column].isSuggestEmpty()) {
                this.cells[row][column] = new GuiCell(true);
                sumBombs--;
            }
        }
    }

    // Возвращаем количество бомб вокруг ячейки
    @Override
    public int checkingAroundCell(int x, int y) {
        int checking = 0;

        if (y > 0 && cells[x][y - 1].isBomb()) checking++;
        if (x > 0 && cells[x - 1][y].isBomb()) checking++;
        if (y > 0 && x > 0 && cells[x - 1][y - 1].isBomb()) checking++;
        if (y + 1 < sumColumn() && cells[x][y + 1].isBomb()) checking++;
        if (x + 1 < sumRow() && cells[x + 1][y].isBomb()) checking++;
        if (x + 1 < sumRow() && y + 1 < sumColumn() && cells[x + 1][y + 1].isBomb()) checking++;
        if (x + 1 < sumRow() && y > 0 && cells[x + 1][y - 1].isBomb()) checking++;
        if (x > 0 && y + 1 < sumColumn() && cells[x - 1][y + 1].isBomb()) checking++;

        return checking;
    }

    /**
     * Открываем пустые ячейки
     */
    @Override
    public void openEmptyCells() {
        int check = 1, sumEmpty = 0;

        while (check != sumEmpty) {
            check = sumEmpty;
            // Проходим поле и проверяем пустые ячейки
            for (int i = 0; i < sumRow(); i++) {
                for (int j = 0; j < sumColumn(); j++) {
                    // Если ячейка пустая и мы её ещё не проверяли
                    if (cells[i][j].isSuggestEmpty() && !cells[i][j].isChecked()) {

                        // Если возле ячейки нет бомб
                        if (checkingAroundCell(i, j) == 0) {
                            sumEmpty++;
                            // Открываем рядом стоящие ячейки
                            clearAroundCell(i, j);
                            // Помечаем данную ячейку как просмотренную
                            cells[i][j].checked();
                        }

                        // Выводим данную ячейку
                        switch (checkingAroundCell(i, j)) {
                            case 8:
                                this.cells[i][j].suggest8();
                                break;
                            case 7:
                                this.cells[i][j].suggest7();
                                break;
                            case 6:
                                this.cells[i][j].suggest6();
                                break;
                            case 5:
                                this.cells[i][j].suggest5();
                                break;
                            case 4:
                                this.cells[i][j].suggest4();
                                break;
                            case 3:
                                this.cells[i][j].suggest3();
                                break;
                            case 2:
                                this.cells[i][j].suggest2();
                                break;
                            case 1:
                                this.cells[i][j].suggest1();
                                break;
                            default:
                                suggest(i, j, false);
                        }
                    }
                }
            }
        }
    }
}
