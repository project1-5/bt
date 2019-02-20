package bt.bt.torrent.compiler;
import bt.torrent.annotation.Consumes;
import bt.torrent.annotation.Produces;
import bt.torrent.compiler.CompilerVisitor;
import bt.torrent.compiler.MessagingAgentCompiler;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class MessaginAgentCompilerTests {

    class TestClass1{
        @Consumes
        private void foo(){

        }
    }

    public class TestClass2{
        @Consumes
        @Produces

        public void bar(){

        }
    }

    @Test(expected = IllegalStateException.class)

    public void privateFunction(){
        MessagingAgentCompiler messsageAgentCompiler = new MessagingAgentCompiler();
        CompilerVisitor compilerVisitor = mock(CompilerVisitor.class);

        messsageAgentCompiler.compileAndVisit(new TestClass1(),compilerVisitor);
    }

    @Test(expected = IllegalStateException.class)

    public void bothConsmuesAndProducesFunction()
    {
        MessagingAgentCompiler messsageAgentCompiler = new MessagingAgentCompiler();
        CompilerVisitor compilerVisitor = mock(CompilerVisitor.class);
        messsageAgentCompiler.compileAndVisit(new TestClass2(),compilerVisitor);
    }

}
