#include <unistd.h>

int main(void)
{
	return execl("java/bin/java", "java/bin/java", "-jar", "Brocessing.jar", (char*)NULL);
}
