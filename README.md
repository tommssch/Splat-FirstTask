# Splat-FirstTask
Программа по поиску текста в файлах в заданной директории .
Результаты поиска выводятся слева в ввиде дерева ,при нажатии на файл справа выводятся все строчки в которых встречался искомый текст.
Навигация происходит с помощью 3 кнопок ВПЕРЁД,НАЗАД,ВЫДЕЛИТЬ ВСЁ, по заданной области.
Результаты выводятся динамически скроллом,чтобы можно было подгружать достаточно большие файлы и не загружать память.
В выводимой области может находится максимум 600 строк, после чего буффер сбрасывается и появляются последние считанные 100 строк.
При поиске файлов используется java.nio.Files.walkFiletree , в каждой директории ищутся файлы с нужным расширением и запускается поток ,который сканирует этот файл.
Для отображение GUI использовался JavaFX фреймворк.
