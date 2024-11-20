package com.dlsc.jfxcentral.data;

import com.dlsc.jfxcentral.data.model.Blog;
import com.dlsc.jfxcentral.data.model.Book;
import com.dlsc.jfxcentral.data.model.Company;
import com.dlsc.jfxcentral.data.model.Coordinates;
import com.dlsc.jfxcentral.data.model.Documentation;
import com.dlsc.jfxcentral.data.model.Download;
import com.dlsc.jfxcentral.data.model.IkonliPack;
import com.dlsc.jfxcentral.data.model.LearnJavaFX;
import com.dlsc.jfxcentral.data.model.LearnMobile;
import com.dlsc.jfxcentral.data.model.LearnRaspberryPi;
import com.dlsc.jfxcentral.data.model.Library;
import com.dlsc.jfxcentral.data.model.LibraryInfo;
import com.dlsc.jfxcentral.data.model.LinksOfTheWeek;
import com.dlsc.jfxcentral.data.model.Member;
import com.dlsc.jfxcentral.data.model.ModelObject;
import com.dlsc.jfxcentral.data.model.News;
import com.dlsc.jfxcentral.data.model.Person;
import com.dlsc.jfxcentral.data.model.Post;
import com.dlsc.jfxcentral.data.model.RealWorldApp;
import com.dlsc.jfxcentral.data.model.Tip;
import com.dlsc.jfxcentral.data.model.Tool;
import com.dlsc.jfxcentral.data.model.Tutorial;
import com.dlsc.jfxcentral.data.model.Utility;
import com.dlsc.jfxcentral.data.model.Video;
import com.dlsc.jfxcentral.data.pull.PullRequest;
import com.dlsc.jfxcentral.data.util.QueryResult;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DataRepository {

    private static final Logger LOG = Logger.getLogger(DataRepository.class.getName());
    public static File REPO_DIRECTORY = new File(System.getProperty("jfxcentral.repo", new File(System.getProperty("user.home"), ".jfxcentralrepo").getAbsolutePath())).getAbsoluteFile();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static boolean testing;
    private static DataRepository instance;
    private final Gson gson = Converters.registerLocalDate(new GsonBuilder()).setPrettyPrinting().create();
    private final List<Library> libraries = new ArrayList<>();
    private final List<Blog> blogs = new ArrayList<>();
    private final List<News> news = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final List<LinksOfTheWeek> linksOfTheWeek = new ArrayList<>();
    private final List<Tip> tips = new ArrayList<>();
    private final List<Tutorial> tutorials = new ArrayList<>();
    private final List<Video> videos = new ArrayList<>();
    private final List<Download> downloads = new ArrayList<>();
    private final List<RealWorldApp> realWorldApps = new ArrayList<>();
    private final List<Tool> tools = new ArrayList<>();
    private final List<Utility> utilities = new ArrayList<>();
    private final List<Company> companies = new ArrayList<>();
    private final List<Person> people = new ArrayList<>();
    private final List<IkonliPack> ikonliPacks = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Documentation> documentation = new ArrayList<>();
    private final List<LearnJavaFX> learnJavaFX = new ArrayList<>();
    private final List<LearnMobile> learnMobile = new ArrayList<>();
    private final List<LearnRaspberryPi> learnRaspberryPi = new ArrayList<>();
    private String homeText;
    private String openJFXText;
    private long cachedPullRequestsTime;
    private List<PullRequest> cachedPullRequests;

    private DataRepository() {
        doLoadData("initial loading of data upon creation of data repository instance");
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }

        return instance;
    }

    public static void setTesting(boolean testing) {
        DataRepository.testing = testing;
    }

    public static boolean isTesting() {
        return DataRepository.testing;
    }

    public void reload() {
        doLoadData("explicit call to refresh method");
    }

    public void clearData() {
        LOG.fine("clearing data");

        homeText = "";
        openJFXText = "";
        getPeople().clear();
        getLibraries().clear();
        getBooks().clear();
        getNews().clear();
        getVideos().clear();
        getBlogs().clear();
        getCompanies().clear();
        getTools().clear();
        getUtilities().clear();
        getRealWorldApps().clear();
        getDownloads().clear();
        getTutorials().clear();
        getTips().clear();
        getLinksOfTheWeek().clear();
        getIkonliPacks().clear();
        getMembers().clear();
        getDocumentation().clear();
        getLearnJavaFX().clear();
        getLearnMobile().clear();
        getLearnRaspberryPi().clear();
    }

    private void doLoadData(String reason) {
        clearData();

        LOG.fine("loading data, reason = " + reason);

        try {
            // plain texts
            homeText = loadString(new File(getRepositoryDirectory(), "intro.md"));
            openJFXText = loadString(new File(getRepositoryDirectory(), "openjfx/intro.md"));

            // collections
            people.addAll(load(getFile("people/people.json"), new TypeToken<List<Person>>() {
            }.getType()));
            books.addAll(load(getFile("books/books.json"), new TypeToken<List<Book>>() {
            }.getType()));
            videos.addAll(load(getFile("videos/videos.json"), new TypeToken<List<Video>>() {
            }.getType()));
            libraries.addAll(load(getFile("libraries/libraries.json"), new TypeToken<List<Library>>() {
            }.getType()));
            news.addAll(load(getFile("news/news.json"), new TypeToken<List<News>>() {
            }.getType()));
            tutorials.addAll(load(getFile("tutorials/tutorials.json"), new TypeToken<List<Tutorial>>() {
            }.getType()));
            blogs.addAll(load(getFile("blogs/blogs.json"), new TypeToken<List<Blog>>() {
            }.getType()));
            companies.addAll(load(getFile("companies/companies.json"), new TypeToken<List<Company>>() {
            }.getType()));
            tools.addAll(load(getFile("tools/tools.json"), new TypeToken<List<Tool>>() {
            }.getType()));
            utilities.addAll(load(getFile("utilities/utilities.json"), new TypeToken<List<Utility>>() {
            }.getType()));
            realWorldApps.addAll(load(getFile("realworld/realworld.json"), new TypeToken<List<RealWorldApp>>() {
            }.getType()));
            downloads.addAll(load(getFile("downloads/downloads.json"), new TypeToken<List<Download>>() {
            }.getType()));
            tips.addAll(load(getFile("tips/tips.json"), new TypeToken<List<Tip>>() {
            }.getType()));
            linksOfTheWeek.addAll(load(getFile("links/links.json"), new TypeToken<List<LinksOfTheWeek>>() {
            }.getType()));
            ikonliPacks.addAll(load(getFile("ikonlipacks/ikonlipacks.json"), new TypeToken<List<IkonliPack>>() {
            }.getType()));
            members.addAll(load(getFile("members/members.json"), new TypeToken<List<Member>>() {
            }.getType()));
            documentation.addAll(load(getFile("documentation/documentation.json"), new TypeToken<List<Documentation>>() {
            }.getType()));
            learnJavaFX.addAll(load(getFile("learn/javafx/learn.json"), new TypeToken<List<LearnJavaFX>>() {
            }.getType()));
            learnMobile.addAll(load(getFile("learn/mobile/learn.json"), new TypeToken<List<LearnMobile>>() {
            }.getType()));
            learnRaspberryPi.addAll(load(getFile("learn/raspberrypi/learn.json"), new TypeToken<List<LearnRaspberryPi>>() {
            }.getType()));

        } catch (Exception e) {
            LOG.severe("error loading data: " + e.getMessage());
        }

        LOG.fine("data loading finished");
    }

    private <T> Collection<T> load(File file, Type type) {
        try (FileReader fr = new FileReader(file, StandardCharsets.UTF_8)) {
            return gson.fromJson(fr, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(String path) {
        return new File(getRepositoryDirectory(), path);
    }

    public Optional<Person> getPersonById(String id) {
        return people.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Company> getCompanyById(String id) {
        return companies.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Library> getLibraryById(String id) {
        return libraries.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Book> getBookById(String id) {
        return books.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<LearnJavaFX> getLearnJavaFXById(String id) {
        return learnJavaFX.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<LearnMobile> getLearnMobileById(String id) {
        return learnMobile.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<LearnRaspberryPi> getLearnRaspberryPiById(String id) {
        return learnRaspberryPi.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Blog> getBlogById(String id) {
        return blogs.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<RealWorldApp> getRealWorldAppById(String id) {
        return realWorldApps.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Tool> getToolById(String id) {
        return tools.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Utility> getUtilityById(String id) {
        return utilities.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Download> getDownloadById(String id) {
        return downloads.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<News> getNewsById(String id) {
        return news.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Video> getVideoById(String id) {
        return videos.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Tutorial> getTutorialById(String id) {
        return tutorials.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Tip> getTipById(String id) {
        return tips.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<LinksOfTheWeek> getLinksOfTheWeekById(String id) {
        return linksOfTheWeek.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<IkonliPack> getIkonliPackById(String id) {
        return ikonliPacks.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Member> getMemberById(String id) {
        return members.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public Optional<Documentation> getDocumentationById(String id) {
        return documentation.stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    public <T extends ModelObject> List<T> getLinkedObjects(ModelObject modelObject, Class<T> clazz) {
        List<T> itemList = getList(clazz);
        List<String> idsList = getIdList(modelObject, clazz);
        return itemList.stream().filter(item -> idsList.contains(item.getId()) || getIdList(item, modelObject.getClass()).contains(modelObject.getId())).collect(Collectors.toList());
    }

    private <T extends ModelObject> List<String> getIdList(ModelObject modelObject, Class<T> clazz) {
        if (clazz.equals(Video.class)) {
            return modelObject.getVideoIds();
        } else if (clazz.equals(Book.class)) {
            return modelObject.getBookIds();
        } else if (clazz.equals(Library.class)) {
            return modelObject.getLibraryIds();
        } else if (clazz.equals(Tutorial.class)) {
            return modelObject.getTutorialIds();
        } else if (clazz.equals(Download.class)) {
            return modelObject.getDownloadIds();
        } else if (clazz.equals(Person.class)) {
            return modelObject.getPersonIds();
        } else if (clazz.equals(Tool.class)) {
            return modelObject.getToolIds();
        } else if (clazz.equals(Utility.class)) {
            return modelObject.getUtilityIds();
        } else if (clazz.equals(RealWorldApp.class)) {
            return modelObject.getAppIds();
        } else if (clazz.equals(News.class)) {
            return modelObject.getNewsIds();
        } else if (clazz.equals(Blog.class)) {
            return modelObject.getBlogIds();
        } else if (clazz.equals(Company.class)) {
            return modelObject.getCompanyIds();
        } else if (clazz.equals(Tip.class)) {
            return modelObject.getTipIds();
        } else if (clazz.equals(LinksOfTheWeek.class)) {
            return modelObject.getLinksOfTheWeekIds();
        } else if (clazz.equals(IkonliPack.class)) {
            return modelObject.getIkonliPackIds();
        } else if (clazz.equals(Member.class)) {
            return modelObject.getMemberIds();
        } else if (clazz.equals(Documentation.class)) {
            return modelObject.getDocumentationIds();
        } else if (clazz.equals(LearnJavaFX.class)) {
            return modelObject.getLearnJavaFXIds();
        } else if (clazz.equals(LearnMobile.class)) {
            return modelObject.getLearnMobileIds();
        } else if (clazz.equals(LearnRaspberryPi.class)) {
            return modelObject.getLearnRaspberryPiIds();
        }

        throw new IllegalArgumentException("unsupported class type: " + clazz.getSimpleName());
    }

    public <T extends ModelObject> List<T> getList(Class<T> clazz) {
        if (clazz.equals(Video.class)) {
            return (List<T>) videos;
        } else if (clazz.equals(Book.class)) {
            return (List<T>) books;
        } else if (clazz.equals(Library.class)) {
            return (List<T>) libraries;
        } else if (clazz.equals(Tutorial.class)) {
            return (List<T>) tutorials;
        } else if (clazz.equals(Download.class)) {
            return (List<T>) downloads;
        } else if (clazz.equals(Person.class)) {
            return (List<T>) people;
        } else if (clazz.equals(Tool.class)) {
            return (List<T>) tools;
        } else if (clazz.equals(Utility.class)) {
            return (List<T>) utilities;
        } else if (clazz.equals(RealWorldApp.class)) {
            return (List<T>) realWorldApps;
        } else if (clazz.equals(News.class)) {
            return (List<T>) news;
        } else if (clazz.equals(Blog.class)) {
            return (List<T>) blogs;
        } else if (clazz.equals(Company.class)) {
            return (List<T>) companies;
        } else if (clazz.equals(Tip.class)) {
            return (List<T>) tips;
        } else if (clazz.equals(LinksOfTheWeek.class)) {
            return (List<T>) linksOfTheWeek;
        } else if (clazz.equals(IkonliPack.class)) {
            return (List<T>) ikonliPacks;
        } else if (clazz.equals(Member.class)) {
            return (List<T>) members;
        } else if (clazz.equals(Documentation.class)) {
            return (List<T>) documentation;
        } else if (clazz.equals(LearnJavaFX.class)) {
            return (List<T>) learnJavaFX;
        } else if (clazz.equals(LearnMobile.class)) {
            return (List<T>) learnMobile;
        } else if (clazz.equals(LearnRaspberryPi.class)) {
            return (List<T>) learnRaspberryPi;
        }

        throw new IllegalArgumentException("unsupported class type: " + clazz.getSimpleName());
    }

    public <T extends ModelObject> T getByID(Class<T> clz, String id) {
        return getList(clz).stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public boolean isValidId(Class<? extends ModelObject> clz, String id) {
        return getList(clz).stream().anyMatch(item -> item.getId().equals(id));
    }

    public List<Video> getVideosByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Video.class);
    }

    public List<Download> getDownloadsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Download.class);
    }

    public List<Book> getBooksByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Book.class);
    }

    public List<Tutorial> getTutorialsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Tutorial.class);
    }

    public List<Blog> getBlogsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Blog.class);
    }

    public List<Library> getLibrariesByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Library.class);
    }

    public List<Tool> getToolsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Tool.class);
    }

    public List<Utility> getUtilitiesByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Utility.class);
    }

    public List<News> getNewsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, News.class);
    }

    public List<Company> getCompaniesByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Company.class);
    }

    public List<RealWorldApp> getRealWorldAppsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, RealWorldApp.class);
    }

    public List<Person> getPeopleByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Person.class);
    }

    public List<Tip> getTipsByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, Tip.class);
    }

    public List<LearnJavaFX> getLearnJavaFXByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, LearnJavaFX.class);
    }

    public List<LearnMobile> getLearnMobileByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, LearnMobile.class);
    }

    public List<LearnRaspberryPi> getLearnRaspberryPiByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, LearnRaspberryPi.class);
    }

    public List<LinksOfTheWeek> getLinksOfTheWeekByModelObject(ModelObject modelObject) {
        return getLinkedObjects(modelObject, LinksOfTheWeek.class);
    }

    public LibraryInfo getLibraryInfo(Library library) {
        try {
            String libraryId = library.getId();
            File file = new File(getRepositoryDirectory(), "libraries/" + libraryId + "/info.json");
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, LibraryInfo.class);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public File getNewsDirectory(News news) {
        return new File(getRepositoryDirectory(), "news/" + DATE_FORMATTER.format(news.getCreatedOn()) + "-" + news.getId());
    }

    public String getNewsText(News news) {
        return loadString(new File(getNewsDirectory(news), "/text.md"));
    }

    public String getLinksOfTheWeekReadMe(LinksOfTheWeek links) {
        return loadString(new File(getRepositoryDirectory(), "links/" + links.getId() + "/readme.md"));
    }

    public void loadLinksOfTheWeekText(LinksOfTheWeek links, StringProperty textProperty) {
        textProperty.set(getLinksOfTheWeekReadMe(links));
    }

    public String getTutorialReadMe(Tutorial tutorial) {
        return loadString(new File(getRepositoryDirectory(), "tutorials/" + tutorial.getId() + "/readme.md"));
    }

    public String getDownloadReadMe(Download download) {
        return loadString(new File(getRepositoryDirectory(), "downloads/" + download.getId() + "/readme.md"));
    }

    public String getBookReadMe(Book book) {
        return loadString(new File(getRepositoryDirectory(), "books/" + book.getId() + "/readme.md"));
    }

    public String getPersonReadMe(Person person) {
        return loadString(new File(getRepositoryDirectory(), "people/" + person.getId() + "/readme.md"));
    }

    public String getMemberReadMe(Member member) {
        return loadString(new File(getRepositoryDirectory(), "members/" + member.getId() + "/readme.md"));
    }

    public String getToolReadMe(Tool tool) {
        return loadString(new File(getRepositoryDirectory(), "tools/" + tool.getId() + "/readme.md"));
    }

    public String getUtilityReadMe(Utility utility) {
        return loadString(new File(getRepositoryDirectory(), "utilities/" + utility.getId() + "/readme.md"));
    }

    public String getTipReadMe(Tip tip) {
        return loadString(new File(getRepositoryDirectory(), "tips/" + tip.getId() + "/readme.md"));
    }

    public String getRealWorldReadMe(RealWorldApp app) {
        return loadString(new File(getRepositoryDirectory(), "realworld/" + app.getId() + "/readme.md"));
    }

    public String getCompanyReadMe(Company company) {
        return loadString(new File(getRepositoryDirectory(), "companies/" + company.getId() + "/readme.md"));
    }

    public String getLibraryReadMe(Library library) {
        return loadString(new File(getRepositoryDirectory(), "libraries/" + library.getId() + "/readme.md"));
    }

    public String getLearnJavaFXReadMe(LearnJavaFX learnJavaFX) {
        return loadString(new File(getRepositoryDirectory(), "learn/javafx/" + learnJavaFX.getId() + "/readme.md"));
    }

    public String getLearnMobileReadMe(LearnMobile learnMobile) {
        return loadString(new File(getRepositoryDirectory(), "learn/mobile/" + learnMobile.getId() + "/readme.md"));
    }

    public String getLearnRaspberryPiReadMe(LearnRaspberryPi learnRaspberryPi) {
        return loadString(new File(getRepositoryDirectory(), "learn/raspberrypi/" + learnRaspberryPi.getId() + "/readme.md"));
    }

    public String getRepositoryDirectoryURL() {
        return getRepositoryDirectory().toURI().toString();
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public List<Blog> getBlogs() {
        return blogs;
    }

    public List<News> getNews() {
        return news;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<LinksOfTheWeek> getLinksOfTheWeek() {
        return linksOfTheWeek;
    }

    public List<Tip> getTips() {
        return tips;
    }

    public List<Tutorial> getTutorials() {
        return tutorials;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public List<Download> getDownloads() {
        return downloads;
    }

    public List<RealWorldApp> getRealWorldApps() {
        return realWorldApps;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public List<Utility> getUtilities() {
        return utilities;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<IkonliPack> getIkonliPacks() {
        return ikonliPacks;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<Documentation> getDocumentation() {
        return documentation;
    }

    public List<LearnJavaFX> getLearnJavaFX() {
        return learnJavaFX;
    }

    public List<LearnMobile> getLearnMobile() {
        return learnMobile;
    }

    public List<LearnRaspberryPi> getLearnRaspberryPi() {
        return learnRaspberryPi;
    }

    public String getHomeText() {
        return homeText;
    }

    public String getOpenJFXText() {
        return openJFXText;
    }

    private String loadString(File file) {
        LOG.fine("loading string from: " + file);

        StringBuilder sb = new StringBuilder();

        try (BufferedReader in = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (MalformedURLException e) {
            LOG.fine("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            LOG.fine("I/O Error: " + e.getMessage());
        }

        return sb.toString();
    }

    public StringProperty getArtifactVersion(Coordinates coordinates) {

        String groupId = coordinates.getGroupId();
        String artifactId = coordinates.getArtifactId();

        StringProperty result = new SimpleStringProperty("");

        if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(artifactId)) {
            loadArtifactVersion(groupId, artifactId, result);
        }

        return result;
    }

    private void loadArtifactVersion(String groupId, String artifactId, StringProperty result) {
        HttpURLConnection con = null;

        try {
            URL url = new URL(MessageFormat.format("https://search.maven.org/solrsearch/select?q=g:{0}+AND+a:{1}", groupId, artifactId));

            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setUseCaches(false);

            int status = con.getResponseCode();
            if (status == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    QueryResult queryResult = gson.fromJson(content.toString(), QueryResult.class);
                    if (!queryResult.getResponse().getDocs().isEmpty()) {
                        result.set(queryResult.getResponse().getDocs().get(0).getLatestVersion());
                    }
                }
            } else {
                result.set("unknown");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public List<Post> loadPosts(Blog blog) {
        LOG.fine("loading posts for blog " + blog.getName());
        try {
            String url = blog.getFeed();
            if (StringUtils.isNotBlank(url)) {
                List<Post> posts = new ArrayList<>();
                URL urlObject = new URL(url);
                URLConnection urlConnection = urlObject.openConnection();
                try (XmlReader reader = new XmlReader(urlConnection.getInputStream())) {
                    SyndFeed feed = new SyndFeedInput().build(reader);
                    List<SyndEntry> entries = feed.getEntries();
                    entries.forEach(entry -> posts.add(new Post(blog, feed, entry)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return posts;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return Collections.emptyList();
    }

    public List<PullRequest> loadPullRequests() {
        long time = System.currentTimeMillis() / 1000;
        long timeToReloadSeconds = 600;
        if (cachedPullRequestsTime + timeToReloadSeconds > time) {
            return cachedPullRequests;
        }
        cachedPullRequestsTime = time;
        cachedPullRequests = loadPullRequestsImpl();
        return cachedPullRequests;

    }

    private List<PullRequest> loadPullRequestsImpl() {
        LOG.fine("loading pull requests");

        HttpURLConnection con = null;

        for (int page = 1; page < 2; page++) {
            try {
                URL url = new URL("https://api.github.com/repos/openjdk/jfx/pulls?state=all&per_page=100&page=" + page);

                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setUseCaches(false);

                int status = con.getResponseCode();
                if (status == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        StringBuilder content = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                        return gson.fromJson(content.toString(), new TypeToken<List<PullRequest>>() {
                        }.getType());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }

        return Collections.emptyList();
    }

    public File getRepositoryDirectory() {
        if (testing) {
            try {
                String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

                // Convert the class file location to the Java file location
                File classFile = new File(path, this.getClass().getName().replace('.', '/') + ".class");
                return classFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
            } catch (URISyntaxException e) {
                LOG.severe("Can not define source root path: " + e.getMessage());
                return new File(System.getProperty("user.dir"));
            }
        }
        return REPO_DIRECTORY;
    }
}
