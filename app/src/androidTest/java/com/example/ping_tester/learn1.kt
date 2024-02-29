import Scanner.util.in

fun main() {

    val scanner = Scanner(System.'in')

    print("Enter your Name")
    val name = scanner.nextLine()
    
    print("Enter your Id number")
    val id = scanner.nextInt()

    print("hello" $name "thanks for ur" $id)

    scanner.close()
}