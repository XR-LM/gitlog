import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    //项目文件路径
    private static final String filePath = "G:\\home\\recordsheet";

    //需要过滤的内容
    private String ignoreContent = "Merge branch";

    public static void main(String[] args) throws IOException, GitAPIException {
        test1();
    }

    public static void test1() throws IOException, GitAPIException {
        Git git = Git.open(new File(filePath));
        Repository repository = git.getRepository();
        List<RevCommit> list = new ArrayList<RevCommit>();
        //获取最近两次提交记录
        LogCommand log = git.log();
        Iterable<RevCommit> iterable = log.setRevFilter(new RevFilter() {
            @Override
            public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
                return cmit.getAuthorIdent().getName().equals("lailai");
            }

            @Override
            public RevFilter clone() {
                return this;
            }
        }).setMaxCount(2).call();

        for (RevCommit revCommit : iterable) {
            list.add(revCommit);
        }
        if (list.size() == 2) {
            RevCommit commit = list.get(0);
            AbstractTreeIterator newCommit = getAbstractTreeIterator(commit, repository);
            RevCommit oldCommitLog = list.get(1);
            AbstractTreeIterator oldCommit = getAbstractTreeIterator(oldCommitLog, repository);

            System.out.println("\n---------------  提交信息 start  -------------------------");
            System.out.println("commit对象:" + commit);
            System.out.println("objectId:" + commit.toObjectId());
            System.out.println("提交人:" + commit.getAuthorIdent().getName());
            System.out.println("提交时间:" + commit.getAuthorIdent().getWhen());
            System.out.println("提交内容:" + commit.getFullMessage());

            //判断两次变化内容
            List<DiffEntry> diff = git.diff().setOldTree(oldCommit).setNewTree(newCommit).call();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DiffFormatter diffFormatter = new DiffFormatter(outputStream);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setRepository(repository);
            for (DiffEntry diffEntry : diff) {
                System.out.println("---------------  发生变化的文件 -------------------------");
                diffFormatter.format(diffEntry);
                System.out.println(outputStream.toString("UTF-8"));
                outputStream.reset();
            }
        }

        git.close();
    }

    public static AbstractTreeIterator getAbstractTreeIterator(RevCommit commit, Repository repository) {
        RevWalk revWalk = new RevWalk(repository);
        CanonicalTreeParser treeParser = null;
        try {
            RevTree revTree = revWalk.parseTree(commit.getTree().getId());
            treeParser = new CanonicalTreeParser();
            treeParser.reset(repository.newObjectReader(), revTree.getId());
            revWalk.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return treeParser;
    }

//    public static void test() {
//        try {
//            File gitWorkDir = new File(filePath);
//            Git git = null;
//            git = Git.open(gitWorkDir);
//            Repository repo = git.getRepository();
//
//            LogCommand log = git.log();
//            log.addPath("web/src/main/resources/pagejs/recordsheet/baseinfo/BomList.js");
//
//            ObjectId lastCommitId = repo.resolve(Constants.HEAD);
//            RevWalk rw = new RevWalk(repo);
//            RevCommit parent = rw.parseCommit(lastCommitId);
//
//            rw.sort(RevSort.COMMIT_TIME_DESC);
//            rw.markStart(parent);
//
//            log.setMaxCount(3);
//            Iterable<RevCommit> logMsgs = log.call();
//            for (RevCommit commit : logMsgs) {
//                System.out.println("\n---------------  提交信息  -------------------------");
//                System.out.println("commit对象:" + commit);
//                System.out.println("objectId:" + commit.toObjectId());
//                System.out.println("提交人:" + commit.getAuthorIdent().getName());
//                System.out.println("提交时间:" + commit.getAuthorIdent().getWhen());
//                System.out.println("提交内容:" + commit.getFullMessage());
//                System.out.println("---DIF STARTING ------------------------");
//
//                //RevTree tree = commit.getTree();DisabledOutputStream.INSTANCE
//
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                // DisabledOutputStream.INSTANCE
//                DiffFormatter df = new DiffFormatter(out);
//                df.setRepository(repo);
//                df.setDiffComparator(RawTextComparator.DEFAULT);
//                df.setDetectRenames(true);
//
//                //df.format(parent.getTree(), commit.getTree());
//                List<DiffEntry> diffs = df.scan(commit.getTree(), commit.getParent(0).getTree()); //df.scan(parent.getTree(), commit.getTree());
//                for (DiffEntry diff : diffs) {
//                    //System.out.println(getCommitMessage());
//                    //df.format(diff);
//                    System.out.println("changeType=" + diff.getChangeType().name()
//                            + " \n newMode=" + diff.getNewMode().getBits()
//                            + " \nnewPath=" + diff.getNewPath()
//                            + " \nold path " + diff.getOldPath()
//                            + " \nHash code " + diff.hashCode()
//                            + " \nString  " + diff.toString()
//                            + " \nchange " + diff.getChangeType().toString()
//                    );
//
//                    df.format(diff);
//                    String diffText = out.toString("UTF-8");
//                    System.out.println(diffText);
//                }
//                df.close();
//                out.close();
//                parent = commit;
//
//            }
//
//        } catch (Exception e) {
//            System.out.println("no head exception : " + e);
//        }
//    }


}
